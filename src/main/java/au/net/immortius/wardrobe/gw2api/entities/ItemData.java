package au.net.immortius.wardrobe.gw2api.entities;

import au.net.immortius.wardrobe.gw2api.Rarity;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import java.util.Set;

/**
 * Information on an "Item" from the gw2 api. This is a combination of the results from multiple endpoints (skins and items), to allow generic processing.
 */
public class ItemData extends CommonData {
    private String name;
    public String icon;
    public Rarity rarity;

    @SerializedName("game_types")
    private Set<String> gameTypes;

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


    public String getName() {
        return Strings.nullToEmpty(name);
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Converts icon path into a file name that can be used locally for caching
     */
    public String getIconName() {
        String[] pathParts = icon.split("/");
        return pathParts[pathParts.length - 2] + "-" + pathParts[pathParts.length - 1];
    }

    /**
     * @return The game types this item is relevant for
     */
    public Set<String> getGameTypes() {
        if (gameTypes == null) {
            gameTypes = Sets.newLinkedHashSet();
        }
        return gameTypes;
    }

    @Override
    public String toString() {
        return getName() + " (" + id + ")";
    }
}
