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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GroupCheck {

    private static Logger logger = LoggerFactory.getLogger(GenerateContent.class);

    private Gson gson;
    private Config config;

    public GroupCheck() throws IOException {
        this(Config.loadConfig());
    }

    public GroupCheck(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
    }


    public static void main(String... args) throws Exception {
        new GroupCheck().run();
    }

    public void run() throws IOException {
        Skins skins = new Skins(config, gson);

        for (UnlockCategoryConfig unlockCategoryConfig : config.unlockCategories) {
            Path groupsFile = config.paths.getGroupsPath().resolve(unlockCategoryConfig.id + ".json");
            if (Files.exists(groupsFile)) {
                try (Reader reader = Files.newBufferedReader(groupsFile)) {
                    CategoryDefinitions categoryDefinitions = gson.fromJson(reader, CategoryDefinitions.class);
                    Set<String> ids = new LinkedHashSet<>();
                    for (Collection<String> groupedIds : categoryDefinitions.getDirectGroups().values()) {
                        ids.addAll(groupedIds);
                    }
                    for (Map<String, Set<String>> subCategories : categoryDefinitions.getTopLevelCategories().values()) {
                        for (Set<String> groupedIds : subCategories.values()) {
                            ids.addAll(groupedIds);
                        }
                    }


                    if (unlockCategoryConfig.source.equals("skins")) {
                        for (String id : ids) {
                            if (!skins.get(id).isPresent()) {
                                System.out.println("Bad " + unlockCategoryConfig.id + " id: " + id);
                            }
                        }
                    } else {
                        for (String id : ids) {
                            Path unlockFile = config.paths.getApiPath().resolve(unlockCategoryConfig.source).resolve(id + ".json");
                            if (!Files.exists(unlockFile)) {
                                System.out.println("Bad " + unlockCategoryConfig.name + " id: " + unlockFile);
                            }
                        }
                    }
                }
            }
        }
    }
}