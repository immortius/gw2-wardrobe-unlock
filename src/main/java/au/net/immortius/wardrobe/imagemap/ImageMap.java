package au.net.immortius.wardrobe.imagemap;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * An image map is a single image containing a number of icons
 */
public class ImageMap implements Iterable<IconDetails> {
    private String name;
    private String image = "";
    private Set<String> contents = Sets.newLinkedHashSet();
    private int mapSize = 2048;
    private int iconSize = 64;
    private transient boolean dirty = false;

    public ImageMap(String name) {
        this.name = name;
    }

    public ImageMap(String name, int mapSize, int iconSize) {
        this(name);
        this.mapSize = mapSize;
        this.iconSize = iconSize;
    }

    public ImageMap(String name, String image, Collection<String> contents) {
        this.name = name;
        this.image = image;
        this.contents.addAll(contents);
    }

    /**
     * @return The name of the image map
     */
    public String getName() {
        return name;
    }

    /**
     * @return The name of the image map image
     */
    public String getImage() {
        return this.image;
    }

    /**
     * @return The icon files in the image map
     */
    public Set<String> getContents() {
        return ImmutableSet.copyOf(contents);
    }

    /**
     * @return Whether the image map is full
     */
    public boolean isFull() {
        return getContents().size() == (mapSize / iconSize) * (mapSize / iconSize);
    }

    /**
     * @return An iterator over the icons composing the image map, with details of their position
     */
    @Override
    public Iterator<IconDetails> iterator() {
        return new Iterator<IconDetails>() {
            private int index = 0;
            private Iterator<String> contentIterator = contents.iterator();
            private int dim = mapSize / iconSize;

            @Override
            public boolean hasNext() {
                return contentIterator.hasNext();
            }

            @Override
            public IconDetails next() {
                int yOffset = (index / dim) * iconSize;
                int xOffset = (index % dim) * iconSize;
                IconDetails next = new IconDetails(name, xOffset, yOffset, contentIterator.next());
                index++;
                return next;
            }
        };
    }

    /**
     * @param iconName The icon to add to this image map
     */
    public void add(String iconName) {
        contents.add(iconName);
        dirty = true;
    }

    /**
     * @return Whether this image map is dirty (has had an icon added)
     */
    public boolean isDirty() {
        return dirty;
    }

    /**
     * Generates the image map image
     * @param iconSourcePath The location containing the source icons
     * @param imageMapPath The location to save the map to
     * @throws IOException
     */
    public void generateMap(Path iconSourcePath, Path imageMapPath) throws IOException {
        image = generateImageName();

        BufferedImage map = new BufferedImage(mapSize, mapSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = map.createGraphics();
        graphics.setColor(Color.BLACK);
        graphics.fillRect(0,0,mapSize, mapSize);

        int xIndex = 0;
        int yIndex = 0;
        int dim = mapSize / iconSize;
        for (String iconName : contents) {
            Path iconPath = iconSourcePath.resolve(iconName);
            if (Files.exists(iconPath)) {
                BufferedImage icon = ImageIO.read(iconPath.toFile());
                graphics.drawImage(icon, xIndex * iconSize, yIndex * iconSize, xIndex * iconSize + iconSize, yIndex * iconSize + iconSize, 0, 0, icon.getWidth(), icon.getHeight(), null);
            }
            xIndex++;
            if (xIndex == dim) {
                xIndex = 0;
                yIndex++;
            }
        }

        JPEGImageWriteParam jpegParams = new JPEGImageWriteParam(null);
        jpegParams.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpegParams.setCompressionQuality(0.7f);

        final ImageWriter writer = ImageIO.getImageWritersByFormatName("jpg").next();
        try (FileImageOutputStream out = new FileImageOutputStream(imageMapPath.resolve(image).toFile())) {
            writer.setOutput(out);
            writer.write(null, new IIOImage(map, null, null), jpegParams);
        }

        graphics.dispose();
    }

    /**
     * Randomise the image name whenever it changes, so that old cached images do not cause issues
     * @return Generates a new name for an image.
     */
    private String generateImageName() {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[40];
        random.nextBytes(bytes);
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        return "im-" + encoder.encodeToString(bytes) + ".jpg";
    }
}
