package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import com.google.gson.Gson;

/**
 * Cache accessor for items
 */
public class Items extends CacheAccessor<ItemData> {

    public Items(Config config, Gson gson) {
        super(gson, ItemData.class, config.paths.getItemPath());
    }

}
