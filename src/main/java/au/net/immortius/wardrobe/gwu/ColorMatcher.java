package au.net.immortius.wardrobe.gwu;

import au.net.immortius.wardrobe.util.ColorUtil;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

/**
 * Matches colors from Guaranteed Wardrobe Unlock preview screenshots to a list of colors (as hex color strings)
 */
public class ColorMatcher implements UnlockMatcher<String> {
    private static final Logger logger = LoggerFactory.getLogger(ColorMatcher.class);

    private final int screenshotIconSize;
    private final int borderSize;

    public ColorMatcher(int screenshotIconSize, int borderSize) {
        this.screenshotIconSize = screenshotIconSize;
        this.borderSize = borderSize;
    }

    public Multiset<Set<String>> matchIcons(Path screenshotRootPath, Set<String> possibleMatches) {
        Multiset<Set<String>> matches = HashMultiset.create();
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

                        Set<String> bestMatches = Sets.newLinkedHashSet();
                        float bestScore = Integer.MAX_VALUE;
                        for (String color : possibleMatches) {
                            int[] dyeColor = ColorUtil.hexToRgb(color);
                            float score = 0;
                            score += Math.abs(dyeColor[0] - r);
                            score += Math.abs(dyeColor[1] - g);
                            score += Math.abs(dyeColor[2] - b);
                            if (score < bestScore && score < 8) {
                                bestScore = score;
                                bestMatches.clear();
                                bestMatches.add(color);
                            } else if (score == bestScore) {
                                bestMatches.add(color);
                            }
                        }
                        if (bestMatches.isEmpty()) {
                            logger.info("No matches for ({}, {}) on {}", i, j, screenshotPath.getFileName());
                        } else {
                            matches.add(bestMatches);
                        }

                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error matching dyes", e);
        }
        return matches;
    }
}
