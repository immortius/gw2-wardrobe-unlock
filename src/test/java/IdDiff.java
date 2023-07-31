import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.Grouping;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.CacheAccessor;
import au.net.immortius.wardrobe.gw2api.Skins;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import au.net.immortius.wardrobe.site.GenerateContent;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

public class IdDiff {

    private static Logger logger = LoggerFactory.getLogger(GenerateContent.class);

    private Gson gson;
    private Config config;

    public IdDiff() throws IOException {
        this(Config.loadConfig());
    }

    public IdDiff(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
    }


    public static void main(String... args) throws Exception {
        new IdDiff().run();
    }

    public void run() throws IOException {
        Skins skins = new Skins(config, gson);

        String category = "back";

        for (UnlockCategoryConfig unlockCategoryConfig : config.unlockCategories) {

            if (category.equals(unlockCategoryConfig.id)) {
                logger.info("-={}=-", unlockCategoryConfig.name);
                Path newFile = Paths.get("new.json");
                Path oldFile = Paths.get("old.json");

                CacheAccessor<ItemData> cache = new CacheAccessor<ItemData>(gson, ItemData.class, config.paths.getApiPath().resolve(unlockCategoryConfig.source));

                Set<String> newIds;
                Set<String> oldIds;
                try (Reader newGwus = Files.newBufferedReader(newFile)) {
                    newIds = gson.fromJson(newGwus, Grouping.class).contents;
                }
                try (Reader oldGwus = Files.newBufferedReader(oldFile)) {
                    oldIds = gson.fromJson(oldGwus, Grouping.class).contents;
                }
                logger.info("Total added: ({})", newIds.size() - oldIds.size());
                if (unlockCategoryConfig.source.equals("skins")) {
                    for (String id : Sets.difference(newIds, oldIds)) {
                        logger.info("+{} ({})", skins.get(id).get().getName(), id);
                    }
                    for (String id : Sets.difference(oldIds, newIds)) {
                        logger.info("-{} ({})", skins.get(id).get().getName(), id);
                    }
                } else {
                    for (String id : Sets.difference(newIds, oldIds)) {
                        logger.info("+{} ({})", cache.get(id).get().getName(), id);
                    }
                    for (String id : Sets.difference(oldIds, newIds)) {
                        logger.info("-{} ({})", cache.get(id).get().getName(), id);
                    }
                }
            }
        }
    }
}
