package au.net.immortius.wardrobe.linkedunlocks;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.gw2api.Unlocks;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import au.net.immortius.wardrobe.gwu.GenerateThumbnails;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class ExtractLinkedArmor {
    private static Logger logger = LoggerFactory.getLogger(GenerateThumbnails.class);

    private final Config config;

    public ExtractLinkedArmor() throws IOException {
        this(Config.loadConfig());
    }

    public ExtractLinkedArmor(Config config) {
        this.config = config;
    }

    public static void main(String... args) throws Exception {
        new ExtractLinkedArmor().run();
    }

    public void run() throws IOException {
        Gson gson = new GsonFireBuilder().createGson();
        Unlocks unlocks = new Unlocks(config, gson);
        ListMultimap<String, ItemData> iconMap = ArrayListMultimap.create();
        unlocks.forEach(config.unlockCategories.stream().filter(x -> x.id.equals("armor")).findFirst().get(), itemData -> {
            iconMap.put(itemData.icon, itemData);
        });
        List<LinkedUnlocks> linkedUnlocksList = new ArrayList<>();
        for (Map.Entry<String, Collection<ItemData>> entry : iconMap.asMap().entrySet()) {
            if (entry.getValue().size() > 1) {
                LinkedUnlocks linkedUnlocks = new LinkedUnlocks();
                Set<String> armorUnlocks = new HashSet<>();
                for (ItemData data : entry.getValue()) {
                    linkedUnlocks.name = data.getName();
                    armorUnlocks.add(data.id);
                }
                logger.info("Created group {}", linkedUnlocks.name);
                linkedUnlocks.primary.put("armor", armorUnlocks);
                linkedUnlocksList.add(linkedUnlocks);
            }
        }

        try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("linkedUnlocks.json"))) {
            gson.toJson(linkedUnlocksList, writer);
        }
    }
}
