import au.net.immortius.wardrobe.config.CategoryDefinitions;
import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.Grouping;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.CacheAccessor;
import au.net.immortius.wardrobe.gw2api.Skins;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import au.net.immortius.wardrobe.site.GenerateContent;
import au.net.immortius.wardrobe.site.entities.UnlockCategoryGroupData;
import au.net.immortius.wardrobe.site.entities.UnlockGroupData;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CleanGroups {

    private static Logger logger = LoggerFactory.getLogger(CleanGroups.class);

    private Gson gson;
    private Config config;

    public CleanGroups() throws IOException {
        this(Config.loadConfig());
    }

    public CleanGroups(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGsonBuilder().setLenient().setPrettyPrinting().create();
    }


    public static void main(String... args) throws Exception {
        new CleanGroups().run();
    }

    public void run() throws IOException {
        for (UnlockCategoryConfig unlockCategoryConfig : config.unlockCategories) {
            Path groupsFile = config.paths.getGroupsPath().resolve(unlockCategoryConfig.id + ".json");
            if (Files.exists(groupsFile)) {
                if (unlockCategoryConfig.nonStandardId) {

                } else {
                    IntCategoryDefinitions categoryDefinitions;
                    try (Reader reader = Files.newBufferedReader(groupsFile)) {
                        categoryDefinitions = gson.fromJson(reader, IntCategoryDefinitions.class);
                    }
                    for (String group : categoryDefinitions.getDirectGroups().keySet()) {
                        categoryDefinitions.getDirectGroups().put(group, categoryDefinitions.getDirectGroups().get(group).stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new)));
                    }
                    for (Map<String, Set<Integer>> groups : categoryDefinitions.getTopLevelCategories().values()) {
                        groups.replaceAll((g, v) -> groups.get(g).stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new)));
                    }

                    try (Writer writer = Files.newBufferedWriter(groupsFile)) {
                        gson.toJson(categoryDefinitions, writer);
                    }
                }
            }
        }
    }

    public static class IntCategoryDefinitions {
        private Map<String, Map<String, Set<Integer>>> topLevelCategories;
        private Map<String, Set<Integer>> directGroups;

        public Map<String, Map<String, Set<Integer>>> getTopLevelCategories() {
            if (topLevelCategories == null) {
                topLevelCategories = new LinkedHashMap<>();
            }
            return topLevelCategories;
        }

        public Map<String, Set<Integer>> getDirectGroups() {
            if (directGroups == null) {
                directGroups = new LinkedHashMap<>();
            }
            return directGroups;
        }
    }
}

