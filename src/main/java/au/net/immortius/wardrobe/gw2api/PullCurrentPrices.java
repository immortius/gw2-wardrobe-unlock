package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.site.entities.Price;
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Obtain the current TP prices of all unlock items
 */
public class PullCurrentPrices {

    private static Logger logger = LoggerFactory.getLogger(PullCurrentPrices.class);
    private static final GenericType<Map<Integer, Collection<Integer>>> UNLOCK_ITEM_MULTIMAP = new GenericType<Map<Integer, Collection<Integer>>>() {};

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

    public PullCurrentPrices(Config config, Client client){
        this.client = client;
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
        this.unlocks = new Unlocks(config, gson);
        this.items = new Items(config, gson);
        this.prices = new Prices(config, gson);
    }

    public static void main(String ... args) throws Exception {
        new PullCurrentPrices().run();
    }

    public void run() throws IOException {
        Files.createDirectories(config.paths.getPricesPath());
        NioUtils.deleteContents(config.paths.getPricesPath());

        Set<Integer> unlockItemIds = Sets.newLinkedHashSet();
        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            try (Reader unlockToSkinMappingReader = Files.newBufferedReader(config.paths.getUnlockItemsPath().resolve(unlockCategory.id + ".json"))) {
                Map<Integer, Collection<Integer>> unlockItems = gson.fromJson(unlockToSkinMappingReader, UNLOCK_ITEM_MULTIMAP.getType());
                unlockItems.values().stream().forEach(x -> {
                    unlockItemIds.addAll(x);
                });
            }
        }

        ApiCacher cacher = new ApiCacher(gson, client);
        Set<Integer> itemIds = Sets.intersection(unlockItemIds, cacher.availableIds(config.prices.apiUrl));
        cacher.cacheIds(config.prices.apiUrl, config.paths.getPricesPath(), itemIds);

        // And now map item prices to unlock prices
        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            Map<Integer, Price> categoryPrices = Maps.newLinkedHashMap();
            try (Reader unlockToSkinMappingReader = Files.newBufferedReader(config.paths.getUnlockItemsPath().resolve(unlockCategory.id + ".json"))) {
                Map<Integer, Collection<Integer>> unlockItems = gson.fromJson(unlockToSkinMappingReader, UNLOCK_ITEM_MULTIMAP.getType());
                unlockItems.entrySet().stream().forEach(unlock -> {
                    int unlockId = unlock.getKey();
                    AtomicInteger minBuyPrice = new AtomicInteger();
                    AtomicInteger minSellPrice = new AtomicInteger();
                    for (int itemId : unlock.getValue()) {
                        prices.get(itemId).ifPresent(x -> {
                            if (x.buys != null && (minBuyPrice.get() == 0 || x.buys.unitPrice < minBuyPrice.get())) {
                                minBuyPrice.set(x.buys.unitPrice);
                            }
                            if (x.sells != null && (minSellPrice.get() == 0 || x.sells.unitPrice < minSellPrice.get())) {
                                minSellPrice.set(x.sells.unitPrice);
                            }

                        });
                    }
                    Price price = new Price();
                    if (minBuyPrice.get() > 0) {
                        price.buyPrice = minBuyPrice.get();
                    }
                    if (minSellPrice.get() > 0) {
                        price.sellPrice = minSellPrice.get();
                    }
                    if (price.sellPrice != null || price.buyPrice != null) {
                        categoryPrices.put(unlockId, price);
                    }
                });
                try (Writer writer = Files.newBufferedWriter(config.paths.getUnlockPricesPath().resolve(unlockCategory.id + ".json"))) {
                    gson.toJson(categoryPrices, writer);
                }
            }
        }
    }




}
