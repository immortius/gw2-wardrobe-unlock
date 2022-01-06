package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.util.NioUtils;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A cacher that downloads gw2 api data - in bulk or pages where possible - saving it as individual files.
 * Will do concurrent downloads where multiple pages are needed.
 */
public class ApiCacher {
    public static final int MINUTE_MS = 60000;
    private static Logger logger = LoggerFactory.getLogger(ApiCacher.class);

    private static GenericType<Set<Integer>> ID_COLLECTION_TYPE = new GenericType<Set<Integer>>() {
    };

    private static final int RATE_LIMIT = 300;

    private static Joiner COMMA_JOINER = Joiner.on(',');

    private Gson gson;
    private Client client;
    private int concurrency = 2;
    private int pageSize = 50;

    private static final EvictingQueue<Long> callTimes = EvictingQueue.create(RATE_LIMIT);

    /**
     * @param gson The gson instance to use to load the data
     * @param client The network client to use to access the api
     */
    public ApiCacher(Gson gson, Client client) {
        this.gson = gson;
        this.client = client;
    }

    /**
     * @param concurrency The limit of concurrent downloads
     */
    public void setConcurrency(int concurrency) {
        this.concurrency = concurrency;
    }

    /**
     * @return The limit of concurrent downloads
     */
    public int getConcurrency() {
        return concurrency;
    }

    /**
     * @return The maximum number of data items to download at once, where downloading all is not supported
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * @param pageSize The maximum number of data items to download at once, where downloading all is not supported
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Cache the data for a GW2 api endpoint
     * @param apiUrl The api url
     * @param toPath The path to save the data
     * @param allSupported Whether ids=all is supported for this endpoint
     * @throws IOException
     */
    public int cache(String apiUrl, Path toPath, boolean allSupported) throws IOException {
        if (Files.exists(toPath) && !NioUtils.isDirectoryEmpty(toPath)) {
            return updateCache(apiUrl, toPath);
        } else {
            return createCache(apiUrl, toPath, allSupported);
        }
    }

    /**
     * Obtain the list of ids available from an api endpoint
     * @param apiUrl The api endpoint to collect ids from
     * @return The list of available ids
     */
    public Set<Integer> availableIds(String apiUrl) {
        try {
            applyRateLimit();
            return client.target(apiUrl).request().get(ID_COLLECTION_TYPE);
        }  catch (InternalServerErrorException e) {
            throw new RuntimeException("Failed to download api ids for '" + apiUrl + "'", e);
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted downloading api ids for '" + apiUrl + "'", e);
        }
    }

    /**
     * Cache a set of ids from an api endpoint
     * @param apiUrl The endpoint url
     * @param toPath The path to cache to
     * @param ids The ids to cache data for
     * @return The amount of data successfully downloaded
     */
    public int cacheIds(String apiUrl, Path toPath, Collection<Integer> ids) {
        logger.info("Updating cache for {}", toPath);
        AtomicInteger downloadCounter = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(concurrency);
        List<Integer> fetchPage = Lists.newArrayList();
        for (int id : ids) {
            if (!Files.exists(toPath.resolve(id + ".json"))) {
                fetchPage.add(id);
                if (fetchPage.size() == 50) {
                    ImmutableList<Integer> fetchIds = ImmutableList.copyOf(fetchPage);
                    executorService.submit(() -> retrieveAndSave(apiUrl, fetchIds, toPath, downloadCounter));
                    fetchPage.clear();
                }
            }
        }
        if (!fetchPage.isEmpty()) {
            executorService.submit(() -> retrieveAndSave(apiUrl, fetchPage, toPath, downloadCounter));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.warn("Download timed out for {}", apiUrl);
            return downloadCounter.get();
        }
        logger.info("Downloaded {} new items to {}", downloadCounter.get(), toPath);
        return downloadCounter.get();
    }

    /**
     * Creates a cache from scratch
     * @param apiUrl The endpoint url to cache
     * @param toPath The location to cache to
     * @param allSupported Whether ids=all is supported by this endpoint
     * @return The amount of data cached
     * @throws IOException
     */
    private int createCache(String apiUrl, Path toPath, boolean allSupported) throws IOException {
        Files.createDirectories(toPath);
        if (allSupported) {
            logger.info("Bulk downloading {}", toPath);
            AtomicInteger downloadCounter = new AtomicInteger();
            retrieveAndSave(apiUrl + "?ids=all", toPath, downloadCounter);
            logger.info("Downloaded {} new items to {}", downloadCounter.get(), toPath);
            return downloadCounter.get();
        } else {
            return updateCache(apiUrl, toPath);
        }
    }

    private int updateCache(String apiUrl, Path toPath) {
        Set<Integer> ids = availableIds(apiUrl);
        return cacheIds(apiUrl, toPath, ids);
    }

    private void retrieveAndSave(String baseUrl, List<Integer> fetchPage, Path baseSavePath, AtomicInteger downloadCounter) {
        String url = baseUrl + "?ids=" + COMMA_JOINER.join(fetchPage);
        retrieveAndSave(url, baseSavePath, downloadCounter);
    }

    private void retrieveAndSave(String url, Path baseSavePath, AtomicInteger downloadCounter) {
        try {
            applyRateLimit();
            String json = client.target(url).request().get(String.class);
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(json);
            JsonArray rootArray = root.getAsJsonArray();
            for (JsonElement element : rootArray) {
                JsonObject obj = element.getAsJsonObject();
                int id = obj.get("id").getAsInt();
                Path outPath = baseSavePath.resolve(id + ".json");
                try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(outPath, Charsets.UTF_8))) {
                    gson.toJson(obj, JsonElement.class, writer); } catch (IOException e) {
                    logger.error("Failed to write file {}", outPath, e);
                }
                downloadCounter.incrementAndGet();
            }
        } catch (RuntimeException e) {
            logger.error("Failed bulk retrieval of {}", url, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void applyRateLimit() throws InterruptedException {
        synchronized (callTimes) {
            if (callTimes.size() == RATE_LIMIT) {
                long earliestCall = callTimes.peek();
                long diff = System.currentTimeMillis() - earliestCall;
                while (diff < MINUTE_MS) {
                    Thread.sleep(MINUTE_MS - diff);
                    diff = System.currentTimeMillis() - earliestCall;
                }
            }
            callTimes.add(System.currentTimeMillis());
        }
    }

}
