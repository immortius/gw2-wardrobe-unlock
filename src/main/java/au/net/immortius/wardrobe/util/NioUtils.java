package au.net.immortius.wardrobe.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

/**
 * File handling utilities
 */
public final class NioUtils {
    private static final Logger logger = LoggerFactory.getLogger(NioUtils.class);

    private NioUtils() {
    }

    /**
     * @param dir The directory to check to see if it contains files
     * @return Whether or no the path contains files
     * @throws IOException
     */
    public static boolean isDirectoryEmpty(Path dir) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
            return !dirStream.iterator().hasNext();
        }
    }

    /**
     * Deletes the direct contents of a path (not subpaths)
     * @param path
     * @throws IOException
     */
    public static void deleteContents(Path path) throws IOException {
        Files.walk(path, 1).sorted(Comparator.reverseOrder()).forEach(x -> {
            if (!Files.isDirectory(x)) {
                try {
                    Files.delete(x);
                } catch (IOException e) {
                    logger.error("Failed to delete {}", x, e);
                }
            }
        });
    }

    public static void copyPathContents(Path path, Path toPath) throws IOException {
        Files.walk(path, 1).forEach(x -> {
            try {
                if (Files.isDirectory(x)) {
                    return;
                }

                Path targetFile = toPath.resolve(x.getFileName());
                if (Files.exists(targetFile) && Files.getLastModifiedTime(targetFile).compareTo(Files.getLastModifiedTime(x)) >= 0) {
                    return;
                }

                Files.copy(x, targetFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                logger.error("Failed to copy {}", x, e);
            }
        });
    }
}
