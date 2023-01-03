package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.entities.EmoteData;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;

public class Emotes {
    private static final Logger logger = LoggerFactory.getLogger(Unlocks.class);

    private Config config;
    private Gson gson;
    private Items items;

    public Emotes(Config config, Gson gson) {
        this.config = config;
        this.gson = gson;
        this.items = new Items(config, gson);
    }

    /**
     * @param category The category of unlocks to iterate
     * @param consumer Comsumes all unlocks of the category
     * @throws IOException
     */
    public void forEach(UnlockCategoryConfig category, BiConsumer<EmoteData, ItemData> consumer) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(config.paths.getApiPath().resolve(category.source))) {
            for (Path unlockSource : ds) {
                try (Reader unlockSourceReader = Files.newBufferedReader(unlockSource)) {
                    EmoteData emoteData = gson.fromJson(unlockSourceReader, EmoteData.class);

                    for (String itemId : emoteData.unlockItems) {
                        Optional<ItemData> itemData = items.get(itemId);
                        if (!itemData.isPresent()) {
                            continue;
                        }
                        if (category.typeFilter != null && !category.typeFilter.equals(itemData.get().type)) {
                            continue;
                        }
                        if (Strings.isNullOrEmpty(itemData.get().getName()) && !category.getForceAdd().contains(itemData.get().id)) {
                            continue;
                        }
                        if (category.getExcludeIds().contains(itemData.get().id)) {
                            continue;
                        }
                        consumer.accept(emoteData, itemData.get());
                    }
                }
            }
        }
    }

    /**
     * @param category The category of unlocks
     * @param id The id to obtain data for
     * @return The requested unlock, or {@link Optional#empty()}
     */
    public Optional<ItemData> get(UnlockCategoryConfig category, int id) {
        Path itemFile = config.paths.getApiPath().resolve(category.source).resolve(id + ".json");
        if (Files.exists(itemFile)) {
            try (Reader itemReader = Files.newBufferedReader(itemFile)) {
                ItemData itemData = gson.fromJson(itemReader, ItemData.class);
                if (category.typeFilter != null && !category.typeFilter.equals(itemData.type)) {
                    return Optional.empty();
                }
                if (Strings.isNullOrEmpty(itemData.getName()) && !category.getForceAdd().contains(id)) {
                    return Optional.empty();
                }
                if (category.getExcludeIds().contains(itemData.id)) {
                    return Optional.empty();
                }
                return Optional.of(itemData);
            } catch (IOException e) {
                logger.error("Failed to load unlock {}", id, e);
                return Optional.empty();
            }
        } else {
            return Optional.empty();
        }
    }
}
