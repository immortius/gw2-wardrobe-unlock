package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.gw2api.entities.PriceData;
import com.google.gson.Gson;

/**
 * Cache accessor for Prices
 */
public class Prices extends CacheAccessor<PriceData> {

    public Prices(Config config, Gson gson) {
        super(gson, PriceData.class, config.paths.getPricesPath());
    }

}
