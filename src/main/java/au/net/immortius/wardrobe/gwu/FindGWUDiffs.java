package au.net.immortius.wardrobe.gwu;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class FindGWUDiffs {
    private static Logger logger = LoggerFactory.getLogger(FindGWUDiffs.class);

    private final Config config;
    private final Gson gson;

    private static final GenericType<Set<Integer>> INTEGER_SET_TYPE = new GenericType<Set<Integer>>() {
    };

    public FindGWUDiffs() throws IOException {
        this(Config.loadConfig());
    }

    public FindGWUDiffs(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
    }

    public static void main(String... args) throws Exception {
        new FindGWUDiffs().run();
    }

    public void run() throws IOException {
        Path diffPath = config.paths.baseCachePath.resolve("gwudiffs");
        Files.createDirectories(diffPath);

        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            Path unlockFile = config.paths.getGuaranteedWardrobeUnlocksPath().resolve(unlockCategory.id + ".json");
            Path oldUnlockFile = config.paths.baseCachePath.resolve("gwu-old").resolve(unlockCategory.id + ".json");
            if (!Files.exists(unlockFile) || !Files.exists(oldUnlockFile)) {
                logger.warn("Did not find gwu files for {}", unlockCategory.id);
                continue;
            }
            Set<Integer> unlockdiffs;
            try (Reader gwuReader = Files.newBufferedReader(unlockFile); Reader gwuOldReader = Files.newBufferedReader(oldUnlockFile)) {
                unlockdiffs = gson.fromJson(gwuReader, INTEGER_SET_TYPE.getType());
                Set<Integer> oldContents = gson.fromJson(gwuOldReader, INTEGER_SET_TYPE.getType());
                unlockdiffs.removeAll(oldContents);
            }
            try (Writer writer = Files.newBufferedWriter(diffPath.resolve(unlockCategory.id + ".json"))) {
                gson.toJson(unlockdiffs, writer);
            }
        }
    }
}
