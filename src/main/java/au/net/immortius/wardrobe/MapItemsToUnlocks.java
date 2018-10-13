package au.net.immortius.wardrobe;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.ItemDetailUnlockMapping;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

/**
 * Generates a mapping of unlocks to the items that unlock them
 */
public class MapItemsToUnlocks {

    private static Logger logger = LoggerFactory.getLogger(MapItemsToUnlocks.class);
    private static final String UNLOCK_TYPE = "Unlock";
    private static final String DYE_DETAIL_TYPE = "Dye";

    private Gson gson;
    private Config config;

    public MapItemsToUnlocks() throws IOException {
        this(Config.loadConfig());
    }

    public MapItemsToUnlocks(Config config) {
        this.gson = new GsonFireBuilder().createGson();
        this.config = config;
    }

    public static void main(String... args) throws Exception {
        new MapItemsToUnlocks().run();
    }

    public void run() throws IOException {
        Map<String, String> skinTypeMapping = Maps.newHashMap();
        Map<String, ListMultimap<Integer, Integer>> itemMappings = Maps.newLinkedHashMap();
        for (UnlockCategoryConfig itemCategory : config.unlockCategories) {
            itemMappings.put(itemCategory.id, ArrayListMultimap.create());
            if (!Strings.isNullOrEmpty(itemCategory.typeFilter)) {
                skinTypeMapping.put(itemCategory.typeFilter, itemCategory.id);
            }
        }

        for (Path itemFile : Files.newDirectoryStream(config.paths.getItemPath())) {
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
                                    Path skinFile = config.paths.getSkinsPath().resolve(id + ".json");
                                    if (Files.exists(skinFile)) {
                                        try (Reader skinReader = Files.newBufferedReader(skinFile)) {
                                            ItemData skin = gson.fromJson(skinReader, ItemData.class);
                                            itemMappings.get(skinTypeMapping.get(skin.type)).put(id, itemData.id);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Files.createDirectories(config.paths.getUnlockItemsPath());
        for (UnlockCategoryConfig itemCategory : config.unlockCategories) {
            try (Writer writer = Files.newBufferedWriter(config.paths.getUnlockItemsPath().resolve(itemCategory.id + ".json"))) {
                gson.toJson(itemMappings.get(itemCategory.id).asMap(), writer);
            }
        }

    }
}
