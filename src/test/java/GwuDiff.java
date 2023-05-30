import au.net.immortius.wardrobe.config.Config;
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
import java.util.Set;

public class GwuDiff {

    private static Logger logger = LoggerFactory.getLogger(GenerateContent.class);

    private static final GenericType<Set<String>> ID_SET = new GenericType<Set<String>>() {
    };

    private Gson gson;
    private Config config;

    public GwuDiff() throws IOException {
        this(Config.loadConfig());
    }

    public GwuDiff(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
    }


    public static void main(String... args) throws Exception {
        new GwuDiff().run();
    }

    public void run() throws IOException {
        Skins skins = new Skins(config, gson);


        for (UnlockCategoryConfig unlockCategoryConfig : config.unlockCategories) {
            logger.info("-={}=-", unlockCategoryConfig.name);
            Path newGwuFile = config.paths.getGuaranteedWardrobeUnlocksPath().resolve(unlockCategoryConfig.id + ".json");
            Path oldGwuFile = config.paths.baseCachePath.resolve("gwu-old").resolve(unlockCategoryConfig.id + ".json");
            CacheAccessor<ItemData> cache = new CacheAccessor<ItemData>(gson, ItemData.class, config.paths.getApiPath().resolve(unlockCategoryConfig.source));

            if (Files.exists(newGwuFile) && Files.exists(oldGwuFile)) {
                Set<String> newIds;
                Set<String> oldIds;
                try (Reader newGwus = Files.newBufferedReader(newGwuFile)) {
                    newIds = gson.fromJson(newGwus, ID_SET.getType());
                }
                try (Reader oldGwus = Files.newBufferedReader(oldGwuFile)) {
                    oldIds = gson.fromJson(oldGwus, ID_SET.getType());
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
