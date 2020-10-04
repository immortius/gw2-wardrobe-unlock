package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.entities.PriceData;
import au.net.immortius.wardrobe.site.entities.TradingPostEntry;
import au.net.immortius.wardrobe.site.entities.PriceEntry;
import au.net.immortius.wardrobe.util.GsonJsonProvider;
import au.net.immortius.wardrobe.util.NioUtils;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Obtain the current TP prices of all unlock items
 */
public class PullCurrentPrices {

    private static final GenericType<Map<Integer, Collection<Integer>>> UNLOCK_ITEM_MULTIMAP = new GenericType<Map<Integer, Collection<Integer>>>() {
    };
    private static Logger logger = LoggerFactory.getLogger(PullCurrentPrices.class);
    private final Client client;
    private final Gson gson;
    private final Config config;
    private final Unlocks unlocks;
    private final Items items;
    private final Prices prices;

    public PullCurrentPrices() throws IOException {
        this(Config.loadConfig());
    }

    public PullCurrentPrices(Config config) {
        this.client = ClientBuilder.newClient();
        this.client.register(GsonJsonProvider.class);
        this.gson = new GsonFireBuilder().createGson();
        this.config = config;
        this.unlocks = new Unlocks(config, gson);
        this.items = new Items(config, gson);
        this.prices = new Prices(config, gson);
    }

    public PullCurrentPrices(Config config, Client client) {
        this.client = client;
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
        this.unlocks = new Unlocks(config, gson);
        this.items = new Items(config, gson);
        this.prices = new Prices(config, gson);
    }

    public static void main(String... args) throws Exception {
        new PullCurrentPrices().run();
    }

    public void run() throws IOException {
        Files.createDirectories(config.paths.getPricesPath());
        NioUtils.deleteContents(config.paths.getPricesPath());

        Set<Integer> unlockItemIds = Sets.newLinkedHashSet();
        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            try (Reader unlockToSkinMappingReader = Files.newBufferedReader(config.paths.getUnlockItemsPath().resolve(unlockCategory.id + ".json"))) {
                Map<Integer, Collection<Integer>> unlockItems = gson.fromJson(unlockToSkinMappingReader, UNLOCK_ITEM_MULTIMAP.getType());
                unlockItems.values().forEach(unlockItemIds::addAll);
            }
        }

        ApiCacher cacher = new ApiCacher(gson, client);
        Set<Integer> itemIds = Sets.intersection(unlockItemIds, cacher.availableIds(config.prices.apiUrl));
        cacher.cacheIds(config.prices.apiUrl, config.paths.getPricesPath(), itemIds);

        // And now map item prices to unlock prices
        Files.createDirectories(config.paths.getUnlockPricesPath());
        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            Map<Integer, TradingPostEntry> categoryPrices = Maps.newLinkedHashMap();
            try (Reader unlockToSkinMappingReader = Files.newBufferedReader(config.paths.getUnlockItemsPath().resolve(unlockCategory.id + ".json"))) {
                Map<Integer, Collection<Integer>> unlockItems = gson.fromJson(unlockToSkinMappingReader, UNLOCK_ITEM_MULTIMAP.getType());
                unlockItems.forEach((key, value) -> {
                    int unlockId = key;
                    PriceEntry minSellPrice = null;
                    PriceEntry minBuyPrice = null;
                    for (int itemId : value) {
                        if (prices.get(itemId).isPresent()) {
                            PriceData x = prices.get(itemId).get();
                            if (x.buys != null && (minBuyPrice == null || x.buys.unitPrice < minBuyPrice.getPrice())) {
                                minBuyPrice = new PriceEntry(itemId, x.buys.unitPrice);
                            }
                            if (x.sells != null && (minSellPrice == null || x.sells.unitPrice < minSellPrice.getPrice())) {
                                minSellPrice = new PriceEntry(itemId, x.sells.unitPrice);
                            }
                        }
                    }
                    if (minSellPrice != null || minBuyPrice != null) {
                        categoryPrices.put(unlockId, new TradingPostEntry(minSellPrice, minBuyPrice));
                    }
                });
                try (Writer writer = Files.newBufferedWriter(config.paths.getUnlockPricesPath().resolve(unlockCategory.id + ".json"))) {
                    gson.toJson(categoryPrices, writer);
                }
            }
        }
    }
}
