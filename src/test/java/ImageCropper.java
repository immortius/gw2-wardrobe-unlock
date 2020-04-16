
import org.junit.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *
 */
public class ImageCropper {

    @Test
    public void cropImages() throws IOException {
        Path inputPath = Paths.get("C:\\Users\\Immortius\\Desktop\\unlocks\\raw");
        Path outputPath = Paths.get("C:\\Users\\Immortius\\Desktop\\unlocks\\out");
        if (!Files.exists(outputPath)) {
            Files.createDirectory(outputPath);
        }

        Files.walkFileTree(inputPath, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
               return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                BufferedImage image = ImageIO.read(file.toFile());
                BufferedImage subimage = image.getSubimage(1072, 496, 396, 550);
                String originalFileName = file.getFileName().toString();
                String coreName = originalFileName.substring(0, originalFileName.length() - 4);
                ImageIO.write(subimage, "png", outputPath.resolve(coreName + ".png").toFile());
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
