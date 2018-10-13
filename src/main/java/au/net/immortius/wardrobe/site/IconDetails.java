package au.net.immortius.wardrobe.site;

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

    public String getImageId() {
        return imageId;
    }

    public int getXOffset() {
        return xOffset;
    }

    public int getYOffset() {
        return yOffset;
    }

    public String getIconFile() {
        return iconFile;
    }
}
