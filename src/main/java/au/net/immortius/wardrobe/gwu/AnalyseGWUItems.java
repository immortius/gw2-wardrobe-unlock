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

/**
 * The core Guaranteed Wardrobe Unlock analyser processes. Generates sets of ids for unlocks that are available
 * from the guaranteed wardrobe unlock.
 */
public class AnalyseGWUItems {
    private static Logger logger = LoggerFactory.getLogger(AnalyseGWUItems.class);

    private final Config config;
    private final Gson gson;
    private final Unlocks unlocks;

    public AnalyseGWUItems() throws IOException {
        this(Config.loadConfig());
    }

    public AnalyseGWUItems(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
        unlocks = new Unlocks(config, gson);
    }

    public static void main(String... args) throws Exception {
        new AnalyseGWUItems().run();
    }

    public void run() throws IOException {
        Files.createDirectories(config.paths.getGuaranteedWardrobeUnlocksPath());

        IconMatcher iconMatcher = new IconMatcher(config.gwuAnalyser.iconSize, config.gwuAnalyser.borderSize, config.gwuAnalyser.scaleFactor);
        ColorMatcher colorMatcher = new ColorMatcher(config.gwuAnalyser.iconSize, config.gwuAnalyser.borderSize);

        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            logger.info("Analysing {} gwu images", unlockCategory.id);
            Set<String> gwuUnlocks = Sets.newLinkedHashSet();
            if (unlockCategory.colorBased) {
                gwuUnlocks.addAll(determineUnlocks(unlockCategory, x -> ColorUtil.rgbToHex(x.cloth.rgb), colorMatcher));
            } else {
                gwuUnlocks.addAll(determineUnlocks(unlockCategory, x -> config.paths.getThumbnailPath().resolve(x.getIconName()), iconMatcher));
            }
            gwuUnlocks.addAll(unlockCategory.getGwuIncludeIds());
            List sortedUnlocks = gwuUnlocks.stream().sorted().collect(Collectors.toList());
            try (Writer writer = Files.newBufferedWriter(config.paths.getGuaranteedWardrobeUnlocksPath().resolve(unlockCategory.id + ".json"))) {
                gson.toJson(sortedUnlocks, writer);
            }
        }

    }

    private <T> Set<String> determineUnlocks(UnlockCategoryConfig unlockCategory, Function<ItemData, T> idFunction, UnlockMatcher<T> unlockMatcher) throws IOException {
        ListMultimap<T, ItemData> unlocksByColor = ArrayListMultimap.create();
        unlocks.forEach(unlockCategory, unlock -> {
            if (!unlockCategory.getGwuIgnoreIds().contains(unlock.id)) {
                unlocksByColor.put(idFunction.apply(unlock), unlock);
            }
        });
        Set<String> results = Sets.newLinkedHashSet();
        Multiset<Set<T>> matches = unlockMatcher.matchIcons(config.paths.baseInputPath.resolve(unlockCategory.id + "-gwu"), unlocksByColor.keySet());
        matches.forEachEntry((colors, count) -> {
            List<ItemData> unlocks = colors.stream().map(unlocksByColor::get).flatMap(List::stream).collect(Collectors.toList());
            if (unlocks.size() != count) {
                logger.warn("Occurrence mismatch - {} occurrences but {} matches - // {} {}", count, unlocks.size(), unlocks.get(0).getName(), unlocks.stream().map(x-> x.id).collect(Collectors.toList()));
            } else {
                results.addAll(unlocks.stream().map(x -> x.id).collect(Collectors.toSet()));
            }
        });
        return results;
    }
}
