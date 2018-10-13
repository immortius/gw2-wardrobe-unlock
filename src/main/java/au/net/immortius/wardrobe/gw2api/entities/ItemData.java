package au.net.immortius.wardrobe.gw2api.entities;

import au.net.immortius.wardrobe.gw2api.Rarity;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Information on an "Item" from the gw2 api. This is a bit of a combination of the results from multiple endpoints, to allow generic processing
 */
public class ItemData extends CommonData {
    public String name;
    public String icon;
    public Rarity rarity;
    public List<String> flags;

    @SerializedName("base_rgb")
    public int[] baseRGB;
    public ColorData cloth;
    public ColorData leather;
    public ColorData metal;
    public ColorData fur;

    @SerializedName("default_skin")
    public int defaultSkin;
    public ItemDetailsData details;

    @SerializedName("unlock_items")
    public int[] unlockItems;
    @SerializedName("item_id")
    public int itemId;

    @SerializedName("chat_link")
    public String chatlink;


    /**
     * @return Convers to icon path into a file name that can be used locally for caching
     */
    public String getIconName() {
        String[] pathParts = icon.split("/");
        return pathParts[pathParts.length - 2] + "-" + pathParts[pathParts.length - 1];
    }
}
