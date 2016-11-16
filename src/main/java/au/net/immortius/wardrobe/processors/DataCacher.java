package au.net.immortius.wardrobe.processors;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonWriter;

import javax.ws.rs.client.Client;
import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class DataCacher {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(DataCacher.class);

    private static GenericType<int[]> INT_ARRAY_TYPE = new GenericType<int[]>() {
    };

    private static final Joiner idJoiner = Joiner.on(',');
    private static final int MAX_CONCURRENT_FETCHES = 4;
    private Client client;
    private Gson gson;

    public DataCacher(Gson gson, Client client) {
        this.client = client;
        this.gson = gson;
    }

    public int downloadData(String url, Path savePath, Set<Integer> skipIds) throws IOException {
        AtomicInteger downloadCounter = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_FETCHES);
        int[] result = client.target(url).request().get(INT_ARRAY_TYPE);
        List<Integer> fetchPage = Lists.newArrayList();
        for (int id : result) {
            if (!skipIds.contains(id)) {
                if (!Files.exists(savePath.resolve(id + ".json"))) {
                    fetchPage.add(id);
                }
                if (fetchPage.size() == 50) {
                    ImmutableList<Integer> fetchIds = ImmutableList.copyOf(fetchPage);
                    executorService.submit(() -> retrieveAndSave(url, fetchIds, savePath, downloadCounter));
                    fetchPage.clear();
                }
            }
        }
        if (!fetchPage.isEmpty()) {
            executorService.submit(() -> retrieveAndSave(url, fetchPage, savePath, downloadCounter));
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.warn("Download timed out");
        }
        return downloadCounter.get();
    }

    private void retrieveAndSave(String baseUrl, List<Integer> fetchPage, Path baseSavePath, AtomicInteger downloadCounter) {
        try {
            String url = baseUrl + "?ids=" + idJoiner.join(fetchPage);
            String json = client.target(url).request().get(String.class);
            JsonParser parser = new JsonParser();
            JsonElement root = parser.parse(json);
            JsonArray rootArray = root.getAsJsonArray();
            for (JsonElement element : rootArray) {
                JsonObject obj = element.getAsJsonObject();
                int id = obj.get("id").getAsInt();
                Path outPath = baseSavePath.resolve(id + ".json");
                try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(outPath, Charsets.UTF_8))) {
                    gson.toJson(obj, JsonElement.class, writer);
                } catch (IOException e) {
                    logger.error("Failed to write file {}", outPath, e);
                }
                downloadCounter.incrementAndGet();
            }
        } catch (RuntimeException e) {
            logger.error("Failed bulk request", e);
        }

    }

}
