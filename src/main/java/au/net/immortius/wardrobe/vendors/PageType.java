package au.net.immortius.wardrobe.vendors;

/**
 * Type of page, determines where it will be cached.
 */
public enum PageType {
    /**
     * A Category page
     */
    CATEGORY("category"),
    /**
     * A vendor page
     */
    VENDOR("vendor"),
    /**
     * A page for an item
     */
    ITEM("item");

    private String path;

    PageType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
