package au.net.immortius.wardrobe.legacy;

import au.net.immortius.wardrobe.legacy.entities.Config;
import au.net.immortius.wardrobe.legacy.entities.Data;
import au.net.immortius.wardrobe.legacy.entities.IconConfig;
import au.net.immortius.wardrobe.legacy.processors.*;
import au.net.immortius.wardrobe.util.GsonJsonProvider;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 */
public class GuaranteedWardrobeUnlockAnalyser {

    private static final Logger logger = LoggerFactory.getLogger(GuaranteedWardrobeUnlockAnalyser.class);

    private static TypeToken<Set<String>> STRING_SET_TYPE = new TypeToken<Set<String>>() {
    };

    private static final Path CACHE_PATH = Paths.get("cache");
    private static final Path INPUT_PATH = Paths.get("input");
    private static final Path SITE_PATH = Paths.get("site");

    private static final Path CONFIG = INPUT_PATH.resolve("config.json");
    private static final Path API_CACHE = CACHE_PATH.resolve("api");
    private static final Path THUMBNAIL_PATH = CACHE_PATH.resolve("thumbs");

    private static final Path WARDROBE_UNLOCK_CONTENTS = SITE_PATH.resolve("guaranteed-wardrobe-unlock-content.json");


    private Client client = ClientBuilder.newClient();
    private DataCacher dataCacher;
    private IconCacher iconCacher = new IconCacher(client);
    private ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();
    private IconMatcher iconMatcher;
    private IdResolver idResolver;
    private Gson gson;

    public static void main(String[] args) throws Exception {
        new GuaranteedWardrobeUnlockAnalyser().run();
    }

    public void run() throws Exception {
        client.register(GsonJsonProvider.class);
        gson = new GsonBuilder().registerTypeAdapter(Class.class, new JsonDeserializer<Class>() {

            @Override
            public Class deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                try {
                    return getClass().getClassLoader().loadClass(json.getAsString());
                } catch (ClassNotFoundException e) {
                    logger.error("Failed to load class '{}'", json.getAsString(), e);
                    return Data.class;
                }
            }
        }).registerTypeAdapter(Predicate.class, new JsonDeserializer<Predicate>() {
            @Override
            public Predicate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                try {
                    return (Predicate) getClass().getClassLoader().loadClass(json.getAsString()).newInstance();
                } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
                    logger.error("Failed to load class '{}'", json.getAsString(), e);
                    return t -> true;
                }
            }
        }).registerTypeAdapter(Function.class, new JsonDeserializer<Function>() {
            @Override
            public Function deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                try {
                    return (Function) getClass().getClassLoader().loadClass(json.getAsString()).newInstance();
                } catch (IllegalAccessException | ClassNotFoundException | InstantiationException e) {
                    logger.error("Failed to load class '{}'", json.getAsString(), e);
                    return t -> t;
                }
            }
        }).create();
        idResolver = new IdResolver(gson);
        dataCacher = new DataCacher(gson, client);

        Config config;
        try (BufferedReader reader = Files.newBufferedReader(CONFIG)) {
            config = gson.fromJson(reader, Config.class);
        }

        iconMatcher = new IconMatcher(config.general.screenshotIconSize, config.general.borderSize, config.general.thumbSize);

        Map<String, List<Integer>> wardrobeData = Maps.newLinkedHashMap();

        for (IconConfig iconConfig : config.iconTypes) {
            Path dataPath = API_CACHE.resolve(iconConfig.getDataId());
            Path manifest = CACHE_PATH.resolve(iconConfig.id + "-manifest.json");
            if (!Files.exists(manifest)) {
                logger.info("Obtaining {} data...", iconConfig.getDataId());
                Files.createDirectories(dataPath);
                int downloaded = dataCacher.downloadData(iconConfig.url, dataPath, iconConfig.getIgnoreIds());
                logger.info("Downloaded {} data files for {}.", downloaded, iconConfig.id);

                logger.info("Obtaining {} icon...", iconConfig.id);
                Path iconPath = API_CACHE.resolve(iconConfig.getIconId());
                Files.createDirectories(iconPath);
                downloaded = iconCacher.cacheIconsForData(dataPath, iconPath, iconConfig.getJavaClass(), iconConfig.getDataFilter());
                logger.info("Downloaded {} {} icon.", downloaded, iconConfig.id);

                Path thumbnailPath = THUMBNAIL_PATH.resolve(iconConfig.getIconId());
                Files.createDirectories(thumbnailPath);
                thumbnailGenerator.generateThumbs(iconPath, thumbnailPath, config.general.thumbSize);

                long start = System.nanoTime();
                Path inputPath = INPUT_PATH.resolve(iconConfig.id);
                Set<String> iconNames = iconMatcher.matchIcons(inputPath, thumbnailPath, iconConfig.getMatchThreshold());
                long end = System.nanoTime();
                logger.info("Time taken: {} seconds", ((double) (end - start)) / 1000000000);
                try (Writer writer = Files.newBufferedWriter(manifest, Charsets.UTF_8)) {
                    gson.toJson(iconNames, STRING_SET_TYPE.getType(), writer);
                } catch (IOException e) {
                    logger.error("Failed to write manifest for {}", iconConfig.id, e);
                }
            }
            List<Integer> ids = Lists.newArrayList(idResolver.collectIds(dataPath, manifest, iconConfig.getJavaClass(), iconConfig.getIgnoreIds(), iconConfig.getIconSharingIds(), iconConfig.getJoinedIds(), iconConfig.getDataFilter(), iconConfig.getMatchFilter()));
            Collections.sort(ids);
            wardrobeData.put(iconConfig.id, ids);
        }

        List<Integer> dyeIds = getDyeIds(config);
        Collections.sort(dyeIds);
        wardrobeData.put(config.dyes.id, dyeIds);

        try (Writer writer = Files.newBufferedWriter(WARDROBE_UNLOCK_CONTENTS, Charsets.UTF_8)) {
            gson.toJson(wardrobeData, Map.class, writer);
        } catch (IOException e) {
            logger.error("Failed to save wardrobe contents", e);
        }

    }

    private List<Integer> getDyeIds(Config config) throws IOException {
        Path dataPath = API_CACHE.resolve(config.dyes.id);
        Files.createDirectories(dataPath);

        logger.info("Obtaining dyes data...");
        int downloaded = dataCacher.downloadData(config.dyes.url, dataPath, Collections.emptySet());
        logger.info("Downloaded {} dyes.", downloaded);

        ColorMatcher matcher = new ColorMatcher(gson, config.general.screenshotIconSize, config.general.borderSize);

        return Lists.newArrayList(matcher.matchColors(INPUT_PATH.resolve(config.dyes.id), dataPath, config.dyes.getIgnoreIds(), config.dyes.getDuplicateColors()));
    }
}

