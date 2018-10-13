package au.net.immortius.wardrobe.config;

import au.net.immortius.wardrobe.util.GsonPathAdapter;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Config {

    public static final Path DEFAULT_CONFIG_PATH = Paths.get("input", "config.json");

    public static Config loadConfig() throws IOException {
        return loadConfig(DEFAULT_CONFIG_PATH);
    }

    public static Config loadConfig(Path path) throws IOException {
        Gson gson = new GsonFireBuilder().createGsonBuilder().registerTypeAdapter(Path.class, new GsonPathAdapter()).create();
        try (Reader configReader = Files.newBufferedReader(path)) {
            return gson.fromJson(configReader, Config.class);
        }
    }

    /**
     * Configuration for file paths
     */
    public PathsConfig paths;

    /**
     * Configuration for GW2 api locations to cache
     */
    public List<CacheConfig> apiCaches;
    /**
     * Configuration for icon to image mapping
     */
    public ImageMapperConfig imageMapper;
    /**
     * Configuration for the unlock to item mapper
     */
    public ItemUnlockMapperConfig itemUnlockMapper;
    /**
     * Configuration of unlock categories to include on the site
     */
    public List<UnlockCategoryConfig> unlockCategories;
}
