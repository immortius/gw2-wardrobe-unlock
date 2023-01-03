package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import au.net.immortius.wardrobe.util.REST;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Caches all icons used by unlocks
 */
public class CacheIcons {
    private static final Logger logger = LoggerFactory.getLogger(CacheIcons.class);

    private final Gson gson;
    private final Config config;
    private final Unlocks unlocks;
    private final Emotes emotes;

    public CacheIcons() throws IOException {
        this(Config.loadConfig());
    }

    public CacheIcons(Config config) {
        this.gson = new GsonFireBuilder().createGson();
        this.config = config;
        this.unlocks = new Unlocks(config, gson);
        this.emotes = new Emotes(config, gson);
    }

    public static void main(String... args) throws Exception {
        new CacheIcons().run();
    }

    public void run() throws IOException {
        Files.createDirectories(config.paths.getIconCachePath());

        Client client = ClientBuilder.newClient();
        AtomicInteger count = new AtomicInteger();
        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            if (unlockCategory.colorBased) {
                continue;
            }
            if (unlockCategory.nonStandardId) {
                emotes.forEach(unlockCategory, (emotesData, itemData) -> {
                    if (cacheIconForItem(config, itemData, client)) {
                        count.incrementAndGet();
                    }
                });
            } else {
                unlocks.forEach(unlockCategory, itemData -> {
                    if (cacheIconForItem(config, itemData, client)) {
                        count.incrementAndGet();
                    }
                });
            }
        }

        logger.info("Downloaded {} new icons", count.get());

    }

    private boolean cacheIconForItem(Config config, ItemData itemData, Client client) {
        Path iconName = config.paths.getIconCachePath().resolve(itemData.getIconName());
        if (!Files.exists(iconName)) {
            try {
                REST.download(client, itemData.icon, config.paths.getIconCachePath().resolve(itemData.getIconName()));
            } catch (NotFoundException e) {
                logger.warn("Unable to download {}", itemData.icon, e);
            }
            return true;
        }
        return false;
    }
}
