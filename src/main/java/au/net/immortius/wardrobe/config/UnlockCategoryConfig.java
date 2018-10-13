package au.net.immortius.wardrobe.config;

import com.google.common.collect.Sets;

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

    private Set<Integer> excludeIds;

    /**
     * @return The ids of any unlocks to exclude from consideration
     */
    public Set<Integer> getExcludeIds() {
        if (excludeIds == null) {
            excludeIds = Sets.newLinkedHashSet();
        }
        return excludeIds;
    }
}
