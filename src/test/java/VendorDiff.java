import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.CacheAccessor;
import au.net.immortius.wardrobe.gw2api.Skins;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import au.net.immortius.wardrobe.site.GenerateContent;
import au.net.immortius.wardrobe.vendors.entities.VendorData;
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
import java.util.List;
import java.util.Set;

public class VendorDiff {

    private static Logger logger = LoggerFactory.getLogger(GenerateContent.class);

    private static final GenericType<List<VendorData>> VENDOR_DATA_LIST_TYPE = new GenericType<List<VendorData>>() {
    };

    private Gson gson;
    private Config config;

    public VendorDiff() throws IOException {
        this(Config.loadConfig());
    }

    public VendorDiff(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
    }


    public static void main(String... args) throws Exception {
        new VendorDiff().run();
    }

    public void run() throws IOException {
        Skins skins = new Skins(config, gson);

        for (UnlockCategoryConfig unlockCategoryConfig : config.unlockCategories) {
            logger.info("-={}=-", unlockCategoryConfig.name);
            Path newGwuFile = config.paths.getVendorsPath().resolve(unlockCategoryConfig.id + ".json");
            Path oldGwuFile = config.paths.baseCachePath.resolve("vendorsold").resolve(unlockCategoryConfig.id + ".json");
            CacheAccessor<ItemData> cache = new CacheAccessor<ItemData>(gson, ItemData.class, config.paths.getApiPath().resolve(unlockCategoryConfig.source));

            if (Files.exists(newGwuFile) && Files.exists(oldGwuFile)) {
                List<VendorData> newIds;
                List<VendorData> oldIds;
                try (Reader newGwus = Files.newBufferedReader(newGwuFile)) {
                    newIds = gson.fromJson(newGwus, VENDOR_DATA_LIST_TYPE.getType());
                }
                try (Reader oldGwus = Files.newBufferedReader(oldGwuFile)) {
                    oldIds = gson.fromJson(oldGwus, VENDOR_DATA_LIST_TYPE.getType());
                }
                logger.info("Total added: ({})", newIds.size() - oldIds.size());

            }
        }
    }
}
