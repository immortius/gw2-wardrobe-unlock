import com.google.common.io.ByteStreams;
import util.REST;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    private static final int MAX_CONCURRENT_FETCHES = 4;
    private Client client;

    public DataCacher(Client client) {
        this.client = client;
    }

    public int downloadData(String url, Path savePath, Set<Integer> skipIds) throws IOException {
        AtomicInteger downloadCounter = new AtomicInteger();
        ExecutorService executorService = Executors.newFixedThreadPool(MAX_CONCURRENT_FETCHES);
        int[] result = client.target(url).request().get(INT_ARRAY_TYPE);
        for (int id : result) {
            if (!skipIds.contains(id)) {
                executorService.submit(() -> retrieveAndSave(url, id, savePath, downloadCounter));
            }
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.warn("Download timed out");
        }
        return downloadCounter.get();
    }

    private void retrieveAndSave(String baseUrl, int id, Path baseSavePath, AtomicInteger downloadCounter) {
        Path targetFile = baseSavePath.resolve(id + ".json");
        if (!Files.exists(targetFile)) {
            String url = baseUrl + "/" + id;
            REST.download(client, url, targetFile);
            downloadCounter.incrementAndGet();
        }
    }


}
