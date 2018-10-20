package au.net.immortius.wardrobe.config;

/**
 * Configuration for mapping an item with a details section to an unlock
 */
public class ItemDetailUnlockMapping {
    /**
     * The type this item detail unlocks - if not resolve from skin
     */
    public String unlockType;
    /**
     * Whether this unlocks a skin, and the exact unlock type should be determined from that skin
     */
    public boolean resolveUnlockTypeFromSkin = false;

    /**
     * The detail 'type' content that is required to trigger this mapping
     */
    public String detailTypeFilter;
    /**
     * The 'unlockType' content that is required to trigger this mapping
     */
    public String unlockTypeFilter;
}
