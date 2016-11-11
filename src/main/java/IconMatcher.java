import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
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
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class IconMatcher {
    private static final Logger logger = LoggerFactory.getLogger(IconMatcher.class);

    private static final int NUM_THREADS = 8;
    private static final int SCREENSHOT_ICON_SIZE = 48;
    private static final int SCREENSHOT_BORDER_SIZE = 10;
    private static final int SCALE_FACTOR = 2;


    public Set<String> matchIcons(Path screenshotRootPath, Path iconCachePath) {
        return matchIcons(screenshotRootPath, iconCachePath, 100);
    }

    public Set<String> matchIcons(Path screenshotRootPath, Path iconCachePath, int threshold) {
        Set<String> iconNames = Sets.newConcurrentHashSet();
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);
        try {
            Map<String, BufferedImage> icons = Maps.newLinkedHashMap();
            try (DirectoryStream<Path> iconFiles = Files.newDirectoryStream(iconCachePath)) {
                for (Path iconFile : iconFiles) {
                    BufferedImage thumb = ImageIO.read(iconFile.toFile());
                    icons.put(iconFile.getFileName().toString(), thumb);
                }
            }
            try (DirectoryStream<Path> screenshotPaths = Files.newDirectoryStream(screenshotRootPath)) {
                for (Path screenshotPath : screenshotPaths) {
                    BufferedImage screen = ImageIO.read(screenshotPath.toFile());
                    int startY = findStart(screen, icons, threshold);

                    int remainingHeight = screen.getHeight() - startY;
                    int columns = 1 + (screen.getWidth() - SCREENSHOT_ICON_SIZE) / (SCREENSHOT_ICON_SIZE + SCREENSHOT_BORDER_SIZE);
                    int rows = 1 + (remainingHeight - SCREENSHOT_ICON_SIZE) / (SCREENSHOT_ICON_SIZE + SCREENSHOT_BORDER_SIZE);
                    for (int j = 0; j < rows; ++j) {
                        for (int i = 0; i < columns; ++i) {
                            BufferedImage mapIcon = scale(screen.getSubimage(i * (SCREENSHOT_ICON_SIZE + SCREENSHOT_BORDER_SIZE), startY + j * (SCREENSHOT_ICON_SIZE + SCREENSHOT_BORDER_SIZE), SCREENSHOT_ICON_SIZE, SCREENSHOT_ICON_SIZE), SCALE_FACTOR);
                            String imageName = "(" + i + ", " + j + ")";
                            executorService.submit(() -> {
                                List<String> matches = Lists.newArrayList();
                                List<BufferedImage> matchIcons = Lists.newArrayList();
                                float currentDiffScore = Float.MAX_VALUE;
                                for (Map.Entry<String, BufferedImage> entry : icons.entrySet()) {
                                    float diffScore = compareImages(mapIcon, entry.getValue(), threshold);
                                    if (diffScore < 1) {
                                        if (Math.abs(diffScore - currentDiffScore) < 0.00001) {
                                            matches.add(entry.getKey());
                                            matchIcons.add(entry.getValue());
                                        } else if (diffScore < currentDiffScore) {
                                            matches.clear();
                                            matchIcons.clear();
                                            matches.add(entry.getKey());
                                            matchIcons.add(entry.getValue());
                                            currentDiffScore = diffScore;
                                        }
                                    }
                                }
                                if (matches.isEmpty()) {
                                    logger.warn("Failed to find match for {} - {}", screenshotPath.getFileName(), imageName);
                                } else if (matches.size() > 1) {
                                    logger.warn("Multiple matches for {} {} - {}", screenshotPath.getFileName(), imageName, matches);
                                } else if (currentDiffScore > 0.5f) {
                                    logger.warn("High match - {} {} to {} - {}", screenshotPath.getFileName(), imageName, matches.get(0), currentDiffScore);
                                 }

                                iconNames.addAll(matches);
                            });
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to process icon matching", e);
        }
        executorService.shutdown();
        try {
            executorService.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            logger.error("Timed out doing icon match calculations, continuing");
        }
        return iconNames;
    }

    private int findStart(BufferedImage screen, Map<String, BufferedImage> icons, int threshold) throws IOException {
        ExecutorService executorService = Executors.newFixedThreadPool(NUM_THREADS);

        Map<Integer, Future<Float>> scores = Maps.newLinkedHashMap();
        for (int offsetY = 0; offsetY < SCREENSHOT_ICON_SIZE + SCREENSHOT_BORDER_SIZE && offsetY < screen.getHeight() - SCREENSHOT_ICON_SIZE; ++ offsetY) {
            int finalOffsetY = offsetY;
            scores.put(offsetY, executorService.submit(() -> {
                float offsetScore = Float.MAX_VALUE;
                BufferedImage firstIcon = scale(screen.getSubimage(0, finalOffsetY, SCREENSHOT_ICON_SIZE, SCREENSHOT_ICON_SIZE), SCALE_FACTOR);
                for (BufferedImage icon : icons.values()) {
                    float diffScore = compareImages(firstIcon, icon, threshold);
                    if (diffScore < offsetScore && diffScore < 1.0) {
                        offsetScore = diffScore;
                    }
                }
                return offsetScore;
            }));
        }
        int bestOffset = 0;
        float bestOffsetScore = Float.MAX_VALUE;
        try {
            for (Map.Entry<Integer, Future<Float>> result : scores.entrySet()) {
                float score = result.getValue().get();
                if (score < bestOffsetScore && score < 1.0) {
                    bestOffset = result.getKey();
                    bestOffsetScore = score;
                }
            }
        } catch (InterruptedException|ExecutionException e) {
            logger.error("Failed to calculate start offset", e);
        }
        executorService.shutdown();
        return bestOffset;
    }

    private BufferedImage scale(BufferedImage image, int scaleFactor) {
        BufferedImage scaled = new BufferedImage(image.getWidth() / scaleFactor, image.getHeight() / scaleFactor, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = scaled.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, scaled.getWidth(), scaled.getHeight(), 0, 0, image.getWidth(), image.getHeight(), null);
        graphics.dispose();
        return scaled;
    }

    private float compareImages(BufferedImage imageA, BufferedImage imageB, float threshold) {
        float diffScore = 0;
        for (int x = 0; x < imageA.getWidth() && diffScore < threshold; ++x) {
            for (int y = 0; y < imageA.getHeight() && diffScore < threshold; y++) {
                diffScore += comparePixel(imageA.getRGB(x, y), imageB.getRGB(x, y));
            }
        }
        return diffScore / threshold;
    }

    private float comparePixel(int rgbA, int rgbB) {
        float diff = 0;
        int aA = (rgbA & 0x80000000) >> 31;
        int aB = (rgbB & 0x80000000) >> 31;
        int rA = (rgbA & 0x00ff0000) >> 16;
        int gA = (rgbA & 0x0000ff00) >> 8;
        int bA = rgbA & 0x000000ff;
        int rB = (rgbB & 0x00ff0000) >> 16;
        int gB = (rgbB & 0x0000ff00) >> 8;
        int bB = rgbB & 0x000000ff;
        diff += (rA - rB) * (rA - rB) / 255f;
        diff += (gA - gB) * (gA - gB) / 255f;
        diff += (bA - bB) * (bA - bB) / 255f;
        return aA * aB * diff / 255f;
    }
}
