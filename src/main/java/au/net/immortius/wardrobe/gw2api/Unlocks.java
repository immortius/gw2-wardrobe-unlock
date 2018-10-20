package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Provides access to unlocks. Takes care of mapping and filtering from gw2 api caches for a given category
 */
public class Unlocks {

    private static final Logger logger = LoggerFactory.getLogger(Unlocks.class);

    private Config config;
    private Gson gson;

    public Unlocks(Config config, Gson gson) {
        this.config = config;
        this.gson = gson;
    }

    /**
     * @param category The category of unlocks to iterate
     * @param consumer Comsumes all unlocks of the category
     * @throws IOException
     */
    public void forEach(UnlockCategoryConfig category, Consumer<ItemData> consumer) throws IOException {
        for (Path unlockSource : Files.newDirectoryStream(config.paths.getApiPath().resolve(category.source))) {
            try (Reader unlockSourceReader = Files.newBufferedReader(unlockSource)) {
                ItemData itemData = gson.fromJson(unlockSourceReader, ItemData.class);
                if (category.typeFilter != null && !category.typeFilter.equals(itemData.type)) {
                    continue;
                }
                if (Strings.isNullOrEmpty(itemData.name)) {
                    continue;
                }
                if (category.getExcludeIds().contains(itemData.id)) {
                    continue;
                }
                consumer.accept(itemData);
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
                if (Strings.isNullOrEmpty(itemData.name)) {
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
