package au.net.immortius.wardrobe;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.gw2api.entities.RecipeData;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Generates an index of all craftable items
 */
public class IndexCraftableItems {

    private static Logger logger = LoggerFactory.getLogger(IndexCraftableItems.class);

    private Gson gson;
    private Config config;

    public IndexCraftableItems() throws IOException {
        this(Config.loadConfig());
    }

    public IndexCraftableItems(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
    }

    public static void main(String... args) throws Exception {
        new IndexCraftableItems().run();
    }

    public void run() throws IOException {
        logger.info("Indexing craftable items");
        Set<Integer> craftableItems = Sets.newLinkedHashSet();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(config.paths.getRecipesPath())) {
            for (Path itemFile : ds) {
                try (Reader reader = Files.newBufferedReader(itemFile)) {
                    RecipeData recipe = gson.fromJson(reader, RecipeData.class);
                    craftableItems.add(recipe.outputItemId);
                }
            }
        }

        List<Integer> items = Lists.newArrayList(craftableItems);
        Collections.sort(items);

        try (Writer writer = Files.newBufferedWriter(config.paths.getCraftableItemsFile())) {
            gson.toJson(items, writer);
        }

    }
}
