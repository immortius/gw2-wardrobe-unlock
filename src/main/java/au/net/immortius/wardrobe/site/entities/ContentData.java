package au.net.immortius.wardrobe.site.entities;

import java.util.List;

/**
 * Root entity for describing the content for the site
 */
public class ContentData {
    public int iconHeight;
    public int iconWidth;
    public List<ImageInfo> images;
    public List<UnlockCategoryData> items;
}
