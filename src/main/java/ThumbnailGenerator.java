import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 *
 */
public class ThumbnailGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ThumbnailGenerator.class);

    public void generateThumbs(Path from, Path to, int thumbSize) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(from)) {
            for (Path iconFile : files) {
                Path outFile = to.resolve(iconFile.getFileName());
                if (!Files.exists(outFile)) {
                    BufferedImage icon = ImageIO.read(iconFile.toFile());
                    BufferedImage thumb = new BufferedImage(thumbSize, thumbSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics = thumb.createGraphics();
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics.drawImage(icon, 0, 0, thumbSize, thumbSize, 0, 0, icon.getWidth(), icon.getHeight(), null);
                    ImageIO.write(thumb, "png", outFile.toFile());
                    graphics.dispose();
                }
            }
        } catch (IOException e) {
            logger.error("Failed to generate thumbs for '{}'", from, e);
        }
    }
}
