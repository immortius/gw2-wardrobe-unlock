package au.net.immortius.wardrobe.config;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Configuration for an Unlock category
 */
public class UnlockCategoryConfig {
    /**
     * The id for the category
     */
    public String id;
    /**
     * The type id to use when generating chat codes
     */
    public int chatcodeType;
    /**
     * Whether to use the id of an item providing the unlock rather than the unlock id when generating the chat code
     */
    public boolean useItemForChatcode;
    /**
     * Whether to use the vendor id as the item id rather than unlock id
      */
    public boolean useItemForVendor;
    /**
     * Display name of the category
     */
    public String name;
    /**
     * The gw2 api to source the unlocks from
     */
    public String source;
    /**
     * The type, if any, to filter items from the gw2 api unlock source by.
     */
    public String typeFilter;
    /**
     * The gw2 api url for obtaining an accounts unlocks from
     */
    public String unlockUrl;

    public boolean nonStandardId;

    /**
     * Is this unlock color based (dyes, basically)
     */
    public boolean colorBased;

    /**
     * Any ids to exclude from the category (typically because they are bugged or are something trivial like the default
     * unlock skin)
     */
    private Set<String> excludeIds;

    /**
     * Mappings of items producing unlocks, for items where the api does not indicate this - allows tp prices to be
     * obtained for the unlocks
     */
    private Map<String, Collection<String>> itemMappings;

    /**
     * Unlock ids to ignore when determining gwu contents
     */
    private Set<String> gwuIgnoreIds;

    /**
     * Unlock ids to ignore when determining bounty contents
     */
    private Set<String> bountyIgnoreIds;

    /**
     * Ids to add even if they lack a name
     */
    private Set<String> forceAdd;

    /**
     * Unlock ids to force include
     */
    private Set<String> gwuIncludeIds;

    /**
     * @return The ids of any unlocks to exclude from consideration
     */
    public Set<String> getExcludeIds() {
        if (excludeIds == null) {
            excludeIds = Sets.newLinkedHashSet();
        }
        return excludeIds;
    }

    /**
     * @return A mapping from items to unlocks they provide in this category
     */
    public Map<String, Collection<String>> getItemMappings() {
        if (itemMappings == null) {
            itemMappings = Maps.newLinkedHashMap();
        }
        return itemMappings;
    }

    public Set<String> getGwuIncludeIds() {
        if (gwuIncludeIds == null) {
            gwuIncludeIds = Sets.newLinkedHashSet();
        }
        return gwuIncludeIds;
    }

    public Set<String> getGwuIgnoreIds() {
        if (gwuIgnoreIds == null) {
            gwuIgnoreIds = Sets.newHashSet();
        }
        return gwuIgnoreIds;
    }

    public Set<String> getBountyIgnoreIds() {
        if (bountyIgnoreIds == null) {
            bountyIgnoreIds = Sets.newHashSet();
        }
        return bountyIgnoreIds;
    }

    public Set<String> getForceAdd() {
        if (forceAdd == null) {
            forceAdd = Sets.newHashSet();
        }
        return forceAdd;
    }
}
