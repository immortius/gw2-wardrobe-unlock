package au.net.immortius.wardrobe.imagemap;

/**
 * Information on an individual icon in an map
 */
public class IconDetails {
    private String imageId;
    private int xOffset;
    private int yOffset;
    private String iconFile;

    public IconDetails(String imageId, int xOffset, int yOffset, String iconFile) {
        this.imageId = imageId;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.iconFile = iconFile;
    }

    /**
     * @return Id of the image map containing the icon
     */
    public String getImageId() {
        return imageId;
    }

    /**
     * @return The x offset of the image in the image map
     */
    public int getXOffset() {
        return xOffset;
    }

    /**
     * @return The y offset of the image in the image map
     */
    public int getYOffset() {
        return yOffset;
    }

    /**
     * @return The name of the icon file
     */
    public String getIconFile() {
        return iconFile;
    }
}
