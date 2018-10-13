package au.net.immortius.wardrobe.legacy.processors;

import au.net.immortius.wardrobe.legacy.entities.Dye;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class ColorMatcher {
    private static final Logger logger = LoggerFactory.getLogger(ColorMatcher.class);

    private final int screenshotIconSize;
    private final int borderSize;
    private final Gson gson;

    public ColorMatcher(Gson gson, int screenshotIconSize, int borderSize) {
        this.gson = gson;
        this.screenshotIconSize = screenshotIconSize;
        this.borderSize = borderSize;
    }

    public Set<Integer> matchColors(Path screenshotRootPath, Path dataPath, Set<Integer> ignoreIds, Set<Integer> duplicateColors) {
        List<Dye> dyes = Lists.newArrayList();
        try (DirectoryStream<Path> dyeFiles = Files.newDirectoryStream(dataPath)) {
            for (Path dyeFile : dyeFiles) {
                try (Reader reader = Files.newBufferedReader(dyeFile)) {
                    Dye dye = gson.fromJson(reader, Dye.class);
                    if (!ignoreIds.contains(dye.id)) {
                        dyes.add(dye);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load dye files", e);
        }
        Set<Integer> foundDyes = Sets.newLinkedHashSet();
        try (DirectoryStream<Path> screenshotPaths = Files.newDirectoryStream(screenshotRootPath, "*.png")) {
            for (Path screenshotPath : screenshotPaths) {
                BufferedImage screen = ImageIO.read(screenshotPath.toFile());
                int columns = 1 + (screen.getWidth() - screenshotIconSize) / (screenshotIconSize + borderSize);
                int rows = 1 + (screen.getHeight() - screenshotIconSize) / (screenshotIconSize + borderSize);
                for (int j = 0; j < rows; ++j) {
                    for (int i = 0; i < columns; ++i) {
                        float r = 0;
                        float g = 0;
                        float b = 0;
                        for (int offX = -3; offX < 4; ++offX) {
                            for (int offY = -3; offY < 4; ++offY) {
                                Color color = new Color(screen.getRGB(screenshotIconSize / 2 + i * (screenshotIconSize + borderSize) + offX, screenshotIconSize / 2 + j * (screenshotIconSize + borderSize) + offY));
                                r += color.getRed();
                                g += color.getGreen();
                                b += color.getBlue();
                            }
                        }
                        r /= 49;
                        g /= 49;
                        b /= 49;

                        List<Dye> bestMatches = Lists.newArrayList();
                        float bestScore = Integer.MAX_VALUE;
                        for (Dye dye : dyes) {
                            float score = 0;
                            score += Math.abs(dye.cloth.rgb[0] - r);
                            score += Math.abs(dye.cloth.rgb[1] - g);
                            score += Math.abs(dye.cloth.rgb[2] - b);
                            if (score < bestScore && score < 6) {
                                bestScore = score;
                                bestMatches.clear();
                                bestMatches.add(dye);
                            } else if (score == bestScore) {
                                bestMatches.add(dye);
                            }
                        }
                        if (bestMatches.size() == 1) {
                            foundDyes.add(bestMatches.get(0).id);
                        } else if (bestMatches.size() > 1) {

                            for (Dye dye : bestMatches) {
                                if (duplicateColors.contains(dye.id)) {
                                    foundDyes.add(dye.id);
                                } else {
                                    logger.info("Multiple match for ({}, {}) from {} - {}({})", i, j, screenshotPath.getFileName(), dye.name, dye.id);
                                }
                            }
                        } else {
                            logger.info("No matches for ({}, {}) on {}", i, j, screenshotPath.getFileName());
                        }

                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error matching dyes", e);
        }
        return foundDyes;
    }
}
