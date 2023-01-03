package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.util.Map;
import java.util.Optional;

/**
 * Cache accessor for skins
 */
public class Skins extends CacheAccessor<ItemData> {

    private final Map<String, String> skinTypeMapping;

    public Skins(Config config, Gson gson) {
        super(gson, ItemData.class, config.paths.getSkinsPath());
        skinTypeMapping = Maps.newHashMap();
        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            if (!Strings.isNullOrEmpty(unlockCategory.typeFilter)) {
                skinTypeMapping.put(unlockCategory.typeFilter, unlockCategory.id);
            }
        }
    }

    public Optional<String> getSkinType(String id) {
        Optional<ItemData> itemData = get(id);
        return itemData.map(this::getSkinType);
    }

    public String getSkinType(ItemData itemData) {
        return skinTypeMapping.get(itemData.type);
    }

}
