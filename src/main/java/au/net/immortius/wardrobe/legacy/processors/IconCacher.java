package au.net.immortius.wardrobe.legacy.processors;

import au.net.immortius.wardrobe.legacy.entities.Data;
import au.net.immortius.wardrobe.util.REST;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

/**
 *
 */
public class IconCacher {

    private static final Logger logger = LoggerFactory.getLogger(IconCacher.class);
    private static final int MAX_CONCURRENT_FETCHES = 4;

    private Gson gson = new GsonBuilder().create();
    private Client client;

    public IconCacher(Client client) {
        this.client = client;
    }

    public int cacheIconsForData(Path dataPath, Path iconCachePath, Class<? extends Data> dataType, Predicate<Data> dataFilter) {
        AtomicInteger counter = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_FETCHES);
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataPath)) {
            for (Path skinFile : files) {
                executorService.submit(() -> {
                    try (JsonReader reader = new JsonReader(Files.newBufferedReader(skinFile))) {
                        Data data = gson.fromJson(reader, dataType);
                        if (!Strings.isNullOrEmpty(data.icon) && dataFilter.test(data)) {
                            Path iconPath = iconCachePath.resolve(data.getIconName());
                            if (!Files.exists(iconPath)) {
                                REST.download(client, data.icon, iconPath);
                                counter.incrementAndGet();
                            }
                        }
                    } catch (IOException e) {
                        logger.warn("Failed to download icon for '{}'", skinFile, e);
                    }
                });
            }
        } catch (IOException e) {
            logger.error("Failed to process {}", dataPath, e);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.warn("Timed out downloading icon, continuing");
        }
        return counter.get();
    }
}
