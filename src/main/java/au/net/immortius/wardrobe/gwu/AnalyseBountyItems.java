package au.net.immortius.wardrobe.gwu;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.Unlocks;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import au.net.immortius.wardrobe.util.ColorUtil;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnalyseBountyItems {
    private static final Logger logger = LoggerFactory.getLogger(AnalyseBountyItems.class);

    private final Config config;
    private final Gson gson;
    private final Unlocks unlocks;

    public AnalyseBountyItems() throws IOException {
        this(Config.loadConfig());
    }

    public AnalyseBountyItems(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
        unlocks = new Unlocks(config, gson);
    }

    public static void main(String... args) throws Exception {
        new AnalyseBountyItems().run();
    }

    public void run() throws IOException {
        Files.createDirectories(config.paths.getBountyUnlocksPath());

        IconMatcher iconMatcher = new IconMatcher(config.gwuAnalyser.iconSize, config.gwuAnalyser.borderSize, config.gwuAnalyser.scaleFactor);
        ColorMatcher colorMatcher = new ColorMatcher(config.gwuAnalyser.iconSize, config.gwuAnalyser.borderSize);

        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            logger.info("Analysing {} bounty images", unlockCategory.id);
            Set<Integer> unlocks = Sets.newLinkedHashSet();
            if (unlockCategory.colorBased) {
                unlocks.addAll(determineUnlocks(unlockCategory, x -> ColorUtil.rgbToHex(x.cloth.rgb), colorMatcher));
            } else {
                unlocks.addAll(determineUnlocks(unlockCategory, x -> config.paths.getThumbnailPath().resolve(x.getIconName()), iconMatcher));
            }
            //unlocks.addAll(unlockCategory.getGwuIncludeIds());
            List<Integer> sortedUnlocks = unlocks.stream().sorted().collect(Collectors.toList());
            try (Writer writer = Files.newBufferedWriter(config.paths.getBountyUnlocksPath().resolve(unlockCategory.id + ".json"))) {
                gson.toJson(sortedUnlocks, writer);
            }
        }

    }

    private <T> Set<Integer> determineUnlocks(UnlockCategoryConfig unlockCategory, Function<ItemData, T> idFunction, UnlockMatcher<T> unlockMatcher) throws IOException {
        ListMultimap<T, ItemData> unlocksByColor = ArrayListMultimap.create();
        unlocks.forEach(unlockCategory, unlock -> {
            if (!unlockCategory.getBountyIgnoreIds().contains(unlock.id)) {
                unlocksByColor.put(idFunction.apply(unlock), unlock);
            }
        });
        Set<Integer> results = Sets.newLinkedHashSet();
        Multiset<Set<T>> matches = unlockMatcher.matchIcons(config.paths.baseInputPath.resolve(unlockCategory.id + "-bounty"), unlocksByColor.keySet());
        matches.forEachEntry((colors, count) -> {
            List<ItemData> unlocks = colors.stream().map(unlocksByColor::get).flatMap(List::stream).collect(Collectors.toList());
            if (unlocks.size() != count) {
                logger.warn("Occurrence mismatch - {} occurrences but {} matches - // {} {}", count, unlocks.size(), unlocks.get(0).getName(), unlocks.stream().map(x-> x.id).collect(Collectors.toList()));
            }
            results.addAll(unlocks.stream().map(x -> x.id).collect(Collectors.toSet()));
        });
        return results;
    }
}
