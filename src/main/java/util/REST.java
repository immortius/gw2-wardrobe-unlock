package util;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
public final class REST {

    private static final Logger logger = LoggerFactory.getLogger(REST.class);

    private REST() {}

    public static void download(Client client, String url, Path toFile) {
        if (!Files.exists(toFile)) {
            try (InputStream in = client.target(url).request().get(InputStream.class); OutputStream out = Files.newOutputStream(toFile)) {
                ByteStreams.copy(in, out);
            } catch (IOException e) {
                logger.error("Failed to download file '{}'", url, e);
            }
        }
    }
}
