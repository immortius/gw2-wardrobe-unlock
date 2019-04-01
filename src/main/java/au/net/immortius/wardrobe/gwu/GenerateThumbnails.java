package au.net.immortius.wardrobe.gwu;

import au.net.immortius.wardrobe.config.Config;
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
 * Generates thumbnails of icons. This is primarily used to reduce the number of pixels that need to be compared
 * per icon
 */
public class GenerateThumbnails {
    private static Logger logger = LoggerFactory.getLogger(GenerateThumbnails.class);

    private final Config config;

    public GenerateThumbnails() throws IOException {
        this(Config.loadConfig());
    }

    public GenerateThumbnails(Config config) {
        this.config = config;
    }

    public static void main(String... args) throws Exception {
        new GenerateThumbnails().run();
    }

    public void run() throws IOException {
        Files.createDirectories(this.config.paths.getThumbnailPath());
        int thumbSize = config.gwuAnalyser.iconSize / config.gwuAnalyser.scaleFactor;

        int count = 0;
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(this.config.paths.getIconCachePath())) {
            for (Path iconFile : ds) {
                Path thumbFile = this.config.paths.getThumbnailPath().resolve(iconFile.getFileName());
                if (!Files.exists(thumbFile)) {
                    BufferedImage icon = ImageIO.read(iconFile.toFile());
                    BufferedImage thumb = new BufferedImage(thumbSize, thumbSize, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D graphics = thumb.createGraphics();
                    graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    graphics.drawImage(icon, 0, 0, thumbSize, thumbSize, 0, 0, icon.getWidth(), icon.getHeight(), null);
                    ImageIO.write(thumb, "png", thumbFile.toFile());
                    graphics.dispose();
                    count++;
                }
            }
        }
        logger.info("Generated {} thumbnails", count);
    }
}
