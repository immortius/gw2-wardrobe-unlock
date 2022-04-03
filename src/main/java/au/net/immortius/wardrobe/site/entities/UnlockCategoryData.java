package au.net.immortius.wardrobe.site.entities;

import java.util.List;

/**
 * Entity describing a category of unlocks for the site
 */
public class UnlockCategoryData {
    public String id;
    public String name;
    public String unlockUrl;
    public List<UnlockCategoryGroupData> categories;
    public List<UnlockGroupData> groups;
}
