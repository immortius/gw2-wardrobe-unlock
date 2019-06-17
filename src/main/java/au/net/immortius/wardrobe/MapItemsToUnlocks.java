package au.net.immortius.wardrobe;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.ItemDetailUnlockMapping;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.Skins;
import au.net.immortius.wardrobe.gw2api.Unlocks;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * Generates a mapping of unlocks to the items that unlock them
 */
public class MapItemsToUnlocks {

    private static Logger logger = LoggerFactory.getLogger(MapItemsToUnlocks.class);

    private Gson gson;
    private Config config;
    private Unlocks unlocks;
    private Skins skins;

    public MapItemsToUnlocks() throws IOException {
        this(Config.loadConfig());
    }

    public MapItemsToUnlocks(Config config) {
        this.gson = new GsonFireBuilder().createGson();
        this.config = config;
        this.unlocks = new Unlocks(config, gson);
        this.skins = new Skins(config, gson);
    }

    public static void main(String... args) throws Exception {
        new MapItemsToUnlocks().run();
    }

    public void run() throws IOException {
        logger.info("Mapping items to unlocks");
        Map<String, String> skinTypeMapping = Maps.newHashMap();
        Map<String, ListMultimap<Integer, Integer>> itemMappings = Maps.newLinkedHashMap();
        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            ListMultimap<Integer, Integer> itemMap = ArrayListMultimap.create();
            itemMappings.put(unlockCategory.id, itemMap);
            if (!Strings.isNullOrEmpty(unlockCategory.typeFilter)) {
                skinTypeMapping.put(unlockCategory.typeFilter, unlockCategory.id);
            }

            for (Map.Entry<Integer, Collection<Integer>> itemUnlocks : unlockCategory.getItemMappings().entrySet()) {
                itemUnlocks.getValue().forEach(unlock -> itemMap.put(unlock, itemUnlocks.getKey()));
            }


            unlocks.forEach(unlockCategory, itemData -> {
                for (int itemId : itemData.getUnlockItems()) {
                    itemMap.put(itemData.id, itemId);
                }
            });
        }

        analyseItems(skinTypeMapping, itemMappings);

        Files.createDirectories(config.paths.getUnlockItemsPath());
        for (UnlockCategoryConfig itemCategory : config.unlockCategories) {
            itemCategory.getExcludeIds().forEach(id -> itemMappings.get(itemCategory.id).removeAll(id));
            try (Writer writer = Files.newBufferedWriter(config.paths.getUnlockItemsPath().resolve(itemCategory.id + ".json"))) {
                gson.toJson(itemMappings.get(itemCategory.id).asMap(), writer);
            }
        }

    }

    private void analyseItems(Map<String, String> skinTypeMapping, Map<String, ListMultimap<Integer, Integer>> itemMappings) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(config.paths.getItemPath())) {
            for (Path itemFile : ds) {
                try (Reader reader = Files.newBufferedReader(itemFile)) {
                    ItemData itemData = gson.fromJson(reader, ItemData.class);
                    if (itemData.defaultSkin != 0) {
                        itemMappings.get(skinTypeMapping.get(itemData.type)).put(itemData.defaultSkin, itemData.id);
                    } else if (itemData.details != null) {
                        for (ItemDetailUnlockMapping unlockDetailsMapping : config.itemUnlockMapper.getUnlockDetailsMappings()) {
                            if (!Objects.equals(itemData.details.type, unlockDetailsMapping.detailTypeFilter)) {
                                continue;
                            }
                            if (unlockDetailsMapping.unlockTypeFilter == null || unlockDetailsMapping.unlockTypeFilter.equals(itemData.details.unlockType)) {
                                if (unlockDetailsMapping.unlockType != null) {
                                    itemMappings.get(unlockDetailsMapping.unlockType).put(itemData.details.colorId, itemData.id);
                                } else if (unlockDetailsMapping.resolveUnlockTypeFromSkin) {
                                    for (int id : itemData.details.skins) {
                                        skins.getSkinType(id).ifPresent(x -> itemMappings.get(x).put(id, itemData.id));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
