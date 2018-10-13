package au.net.immortius.wardrobe.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * File handling utilities
 */
public final class NioUtils {
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
}
