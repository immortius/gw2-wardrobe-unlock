package au.net.immortius.wardrobe.gw2api.entities;

import au.net.immortius.wardrobe.gw2api.Rarity;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.annotations.SerializedName;

import java.util.*;

/**
 * Information on an "Item" from the gw2 api. This is a combination of the results from multiple endpoints (skins and items), to allow generic processing.
 */
public class ItemData extends CommonData {

    private static final Map<String, String> ARMOR_TYPE_MAP = ImmutableMap.<String, String>builder()
            .put("Helm", "Headgear")
            .put("Coat", "Chest")
            .put("Gloves", "Gloves")
            .put("Leggings", "Leggings")
            .put("Boots", "Boots")
            .put("Shoulders", "Shoulders")
            .put("HelmAquatic", "Aquatic Headgear")
            .build();

    private static final Map<String, String> WEAPON_TYPE_MAP = ImmutableMap.<String, String>builder()
            .put("Axe", "Axe")
            .put("Dagger", "Dagger")
            .put("Mace", "Mace")
            .put("Pistol", "Pistol")
            .put("Sword", "Sword")
            .put("Scepter", "Scepter")
            .put("Focus", "Focus")
            .put("Shield", "Shield")
            .put("Torch", "Torch")
            .put("Warhorn", "Warhorn")
            .put("Greatsword", "Greatsword")
            .put("Hammer", "Hammer")
            .put("Longbow", "Longbow")
            .put("Rifle", "Rifle")
            .put("Shortbow", "Short bow")
            .put("Staff", "Staff")
            .put("Spear", "Spear")
            .put("Trident", "Trident")
            .put("Speargun", "Harpoon gun")
            .build();

    private static final Map<String, String> MOUNT_TYPE_MAP = ImmutableMap.<String, String>builder()
            .put("raptor", "Raptor")
            .put("springer", "Springer")
            .put("skimmer", "Skimmer")
            .put("jackal", "Jackal")
            .put("griffon", "Griffon")
            .put("roller_beetle", "Roller Beetle")
            .put("skyscale", "Skyscale")
            .put("turtle", "Siege Turtle")
            .put("warclaw", "Warclaw")
            .build();

    private static final Map<String, String> NOVELTY_TYPE_MAP = ImmutableMap.<String, String>builder()
            .put("Chair", "Chair")
            .put("Tonic", "Tonic")
            .put("Music", "Musical Instrument")
            .put("HeldItem", "Held Item")
            .put("Miscellaneous", "Toy")
            .build();

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
    public String defaultSkin;
    public ItemDetailsData details;

    @SerializedName("unlock_items")
    private String[] unlockItems;
    @SerializedName("unlock_item")
    private String[] unlockItem;
    @SerializedName("item_id")
    private String itemId;

    public String slot;
    public String mount;


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

    public List<String> getUnlockItems() {
        List<String> unlockItems = Lists.newArrayList();
        if (unlockItem != null) {
            unlockItems.addAll(Arrays.asList(unlockItem));
        }
        if (this.unlockItems != null) {
            unlockItems.addAll(Arrays.asList(this.unlockItems));
        }
        if (itemId != null && !itemId.isEmpty() && !itemId.equals("0")) {
            unlockItems.add(itemId);
        }
        return unlockItems;
    }

    @Override
    public String toString() {
        return getName() + " (" + id + ")";
    }

    public String getType() {
        if (slot != null) {
            return NOVELTY_TYPE_MAP.get(slot);
        }
        if (mount != null) {
            return MOUNT_TYPE_MAP.get(mount);
        }
        if (type == null) {
            return "";
        }
        return switch (type) {
            case "Armor" -> details.weightClass + " " + ARMOR_TYPE_MAP.get(details.type);
            case "Weapon" -> WEAPON_TYPE_MAP.get(details.type);
            case "Back" -> "Back Item";
            case "Gathering" -> details.type;
            case "Hero" -> details.type;
            default -> "";
        };
    }
}
