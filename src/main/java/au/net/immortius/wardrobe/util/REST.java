package au.net.immortius.wardrobe.util;

import com.google.common.io.ByteStreams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * REST utility methods
 */
public final class REST {

    private static final Logger logger = LoggerFactory.getLogger(REST.class);

    private REST() {}

    /**
     * A quick and dirty download file to disk method
     * @param client The client to use for the download
     * @param url The url to download
     * @param toFile The path to save the download to
     */
    public static boolean download(Client client, String url, Path toFile) {
        if (!Files.exists(toFile)) {
            try {
                try (InputStream in = client.target(url).request().get(InputStream.class); OutputStream out = Files.newOutputStream(toFile)) {
                    ByteStreams.copy(in, out);
                } catch (IOException e) {
                    logger.error("Failed to download file '{}'", url, e);
                    return false;
                }
            } catch (InternalServerErrorException e) {
                logger.error("Failed to download file '{}'", url, e);
                return false;
            } catch (NotFoundException e) {
                logger.error("Failed to download file '{}'", url);
                return false;
            }
        }
        return true;
    }
}
