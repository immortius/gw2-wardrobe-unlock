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

    /**
     * Is this unlock color based (dyes, basically)
     */
    public boolean colorBased;

    /**
     * Any ids to exclude from the category (typically because they are bugged or are something trivial like the default
     * unlock skin)
     */
    private Set<Integer> excludeIds;

    /**
     * Mappings of items producing unlocks, for items where the api does not indicate this - allows tp prices to be
     * obtained for the unlocks
     */
    private Map<Integer, Collection<Integer>> itemMappings;

    /**
     * Unlock ids to ignore when determining gwu contents
     */
    private Set<Integer> gwuIgnoreIds;

    /**
     * Ids to add even if they lack a name
     */
    private Set<Integer> forceAdd;

    /**
     * Unlock ids to force include
     */
    private Set<Integer> gwuIncludeIds;

    /**
     * @return The ids of any unlocks to exclude from consideration
     */
    public Set<Integer> getExcludeIds() {
        if (excludeIds == null) {
            excludeIds = Sets.newLinkedHashSet();
        }
        return excludeIds;
    }

    /**
     * @return A mapping from items to unlocks they provide in this category
     */
    public Map<Integer, Collection<Integer>> getItemMappings() {
        if (itemMappings == null) {
            itemMappings = Maps.newLinkedHashMap();
        }
        return itemMappings;
    }

    public Set<Integer> getGwuIncludeIds() {
        if (gwuIncludeIds == null) {
            gwuIncludeIds = Sets.newLinkedHashSet();
        }
        return gwuIncludeIds;
    }

    public Set<Integer> getGwuIgnoreIds() {
        if (gwuIgnoreIds == null) {
            gwuIgnoreIds = Sets.newHashSet();
        }
        return gwuIgnoreIds;
    }

    public Set<Integer> getForceAdd() {
        if (forceAdd == null) {
            forceAdd = Sets.newHashSet();
        }
        return forceAdd;
    }
}
