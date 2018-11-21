import au.net.immortius.wardrobe.gw2api.entities.ColorData;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import au.net.immortius.wardrobe.gw2api.entities.RecipeData;
import au.net.immortius.wardrobe.config.Grouping;
import au.net.immortius.wardrobe.imagemap.IconDetails;
import au.net.immortius.wardrobe.imagemap.ImageMap;
import au.net.immortius.wardrobe.site.entities.*;
import au.net.immortius.wardrobe.util.ColorUtil;
import au.net.immortius.wardrobe.vendors.entities.VendorData;
import au.net.immortius.wardrobe.vendors.entities.VendorItem;
import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import io.gsonfire.GsonFireBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DeconstructContent {

    private static Logger logger = LoggerFactory.getLogger(DeconstructContent.class);

    private final Map<String, String> typeToApiMapping = ImmutableMap.<String, String>builder()
            .put("armor", "skins")
            .put("weapon", "skins")
            .put("back", "skins")
            .put("finisher", "finishers")
            .put("glider", "gliders")
            .put("mail", "mailcarriers")
            .put("mini", "minis")
            .put("outfit", "outfits")
            .put("tool", "skins")
            .put("dye", "dyes").build();

    private final Map<String, String> oldTypeToNewMapping = ImmutableMap.<String, String>builder()
            .put("armor", "armor")
            .put("weapon", "weapon")
            .put("back", "back")
            .put("finishers", "finisher")
            .put("gliders", "glider")
            .put("mail", "mail")
            .put("minis", "mini")
            .put("outfits", "outfit")
            .put("tool", "tool")
            .put("dyes", "dye").build();

    @Test
    public void deconstructGroups() throws Exception {
        Path contentPath = Paths.get("input", "content.json");
        Path outRootPath = Paths.get("cache", "extract");

        Gson gson = new GsonFireBuilder().createGsonBuilder().setPrettyPrinting().create();

        try (Reader reader = Files.newBufferedReader(contentPath)) {
            ContentData data = gson.fromJson(reader, ContentData.class);
            for (UnlockCategoryData itemCategory : data.items) {
                Path categoryOutPath = outRootPath.resolve(oldTypeToNewMapping.get(itemCategory.id) + "-groups");
                Files.createDirectories(categoryOutPath);
                for (UnlockGroupData groupData : itemCategory.groups) {
                    Grouping group = new Grouping();
                    group.name = groupData.groupName;
                    group.contents = groupData.content.stream().map(x -> x.id).collect(Collectors.toSet());
                    try (Writer writer = Files.newBufferedWriter(categoryOutPath.resolve(groupData.groupName + ".json"))) {
                        gson.toJson(group, writer);
                    }
                }
            }
        }
    }

    @Test
    public void deconstructAcquisitionMethods() throws Exception {
        Path recipesPath = Paths.get("cache", "api", "recipes");
        Path itemsPath = Paths.get("cache", "api", "items");
        Path contentPath = Paths.get("input", "content.json");
        Path outRootPath = Paths.get("cache", "extract");

        Gson gson = new GsonFireBuilder().createGsonBuilder().setPrettyPrinting().create();

        Set<String> skinCategories = ImmutableSet.of("armor", "weapon", "back");
        Set<String> ignoreTypes = ImmutableSet.of("CraftingMaterial","Trophy","UpgradeComponent","Bag","Trinket","Gizmo","Container","Consumable");
        Set<String> ignoreDetailTypes = ImmutableSet.of("Food","Utility");

        Set<Integer> craftableSkins = Sets.newHashSet();
        for (Path recipePath : Files.newDirectoryStream(recipesPath)) {
            try (Reader recipeReader = Files.newBufferedReader(recipePath)) {
                RecipeData recipe = gson.fromJson(recipeReader, RecipeData.class);
                Path itemPath = itemsPath.resolve(recipe.outputItemId + ".json");
                try (Reader itemReader = Files.newBufferedReader(itemPath)) {
                    ItemData item = gson.fromJson(itemReader, ItemData.class);
                    if (item.defaultSkin != 0) {
                        craftableSkins.add(item.defaultSkin);
                        continue;
                    }
                    if (ignoreTypes.contains(item.type)) {
                        continue;
                    }
                    if (item.details != null && item.details.guildUpgradeId != 0) {
                        continue;
                    }
                    if (item.details != null && ignoreDetailTypes.contains(item.details.type)) {
                        continue;
                    }

                    logger.warn("Failed to find skin for {} ({})", item.name, item.id);
                }
            }
        }


        try (Reader reader = Files.newBufferedReader(contentPath)) {
            ContentData data = gson.fromJson(reader, ContentData.class);
            for (UnlockCategoryData itemCategory : data.items) {
                Path categoryOutPath = outRootPath.resolve(oldTypeToNewMapping.get(itemCategory.id) + "-acquisition");
                Files.createDirectories(categoryOutPath);
                boolean skinCategory = skinCategories.contains(itemCategory.id);

                Map<String, JsonWriter> methodWriters = Maps.newLinkedHashMap();
                for (UnlockGroupData groupData : itemCategory.groups) {
                    Multimap<String, Integer> groupItems = ArrayListMultimap.create();
                    for (UnlockData unlockData : groupData.content) {
                        Set<String> inferredMethods = Sets.newHashSet();
                        inferredMethods.add("gwu");
                        for (VendorInfo vendor : unlockData.getVendors()) {
                            inferredMethods.addAll(vendor.cost.stream().map(x -> x.type).collect(Collectors.toList()));
                        }

                        if (unlockData.priceData != null) {
                            inferredMethods.add("gold");
                            inferredMethods.add("tp");
                        }
                        if (skinCategory && craftableSkins.contains(unlockData.id)) {
                            inferredMethods.add("craft");
                        }
                        for (String source : unlockData.sources) {
                            if (!inferredMethods.contains(source)) {
                                groupItems.put(source, unlockData.id);
                            }
                        }
                    }

                    for (String source : groupItems.keySet()) {
                        JsonWriter writer = methodWriters.get(source);
                        if (writer == null) {
                            writer = new JsonWriter(Files.newBufferedWriter(categoryOutPath.resolve(source + ".json")));
                            writer.setIndent("  ");
                            methodWriters.put(source, writer);
                            writer.beginObject();
                            writer.name("name");
                            writer.value(source);
                            writer.name("contents");
                            writer.beginArray();
                        }
                        writer.jsonValue("// " + groupData.groupName);

                        for (Integer id : groupItems.get(source)) {
                            writer.value(id.intValue());
                            writer.setIndent("");
                        }
                        writer.setIndent("  ");
                    }
                }
                for (JsonWriter writer : methodWriters.values()) {
                    writer.endArray();
                    writer.endObject();
                    writer.flush();
                    writer.close();
                }
            }
        }
    }

    @Test
    public void deconstructVendors() throws IOException {
        Path contentPath = Paths.get("input", "content.json");
        Path outPath = Paths.get("cache", "vendors");
        Files.createDirectories(outPath);

        Gson gson = new GsonFireBuilder().createGsonBuilder().create();

        try (Reader reader = Files.newBufferedReader(contentPath)) {
            ContentData data = gson.fromJson(reader, ContentData.class);
            for (UnlockCategoryData itemCategory : data.items) {
                Map<String, VendorData> vendors = Maps.newLinkedHashMap();
                for (UnlockGroupData groupData : itemCategory.groups) {
                    for (UnlockData unlockData : groupData.content) {
                        if (!unlockData.getVendors().isEmpty()) {
                            for (VendorInfo vendor : unlockData.getVendors()) {
                                VendorData outVendor = vendors.get(vendor.vendorUrl);
                                if (outVendor == null) {
                                    outVendor = new VendorData();
                                    outVendor.name = vendor.vendorName;
                                    outVendor.url = vendor.vendorUrl;
                                    outVendor.items = Lists.newArrayList();
                                    vendors.put(vendor.vendorUrl, outVendor);
                                }
                                VendorItem item = new VendorItem();
                                item.id = unlockData.id;
                                item.cost = vendor.cost;
                                outVendor.items.add(item);
                            }
                        }
                    }
                }
                try (Writer writer = Files.newBufferedWriter(outPath.resolve(oldTypeToNewMapping.get(itemCategory.id) + ".json"))) {
                    gson.toJson(vendors.values(), writer);
                }
            }
        }



    }

    @Test
    public void deconstructPrices() throws IOException {
        Path contentPath = Paths.get("input", "content.json");
        Path outPath = Paths.get("cache", "prices");
        Files.createDirectories(outPath);

        Gson gson = new GsonFireBuilder().createGsonBuilder().create();


        try (Reader reader = Files.newBufferedReader(contentPath)) {
            ContentData data = gson.fromJson(reader, ContentData.class);
            for (UnlockCategoryData itemCategory : data.items) {
                Map<Integer, Price> categoryLookup = Maps.newLinkedHashMap();
                for (UnlockGroupData groupData : itemCategory.groups) {
                    for (UnlockData unlockData : groupData.content) {
                        if (unlockData.priceData != null) {
                            categoryLookup.put(unlockData.id, unlockData.priceData);
                        }
                    }
                }
                try (Writer writer = Files.newBufferedWriter(outPath.resolve(oldTypeToNewMapping.get(itemCategory.id) + ".json"))) {
                    gson.toJson(categoryLookup, writer);
                }
            }
        }



    }

    @Test
    public void deconstructImageMaps() throws IOException {
        Path contentPath = Paths.get("input", "content.json");
        Path outPath = Paths.get("cache", "imagemaps");
        Files.createDirectories(outPath);

        Map<String, String> dataMapping = ImmutableMap.<String, String>builder()
                .put("armor", "skins")
                .put("weapon", "skins")
                .put("back", "skins")
                .put("finisher", "finishers")
                .put("glider", "gliders")
                .put("mail", "mailcarriers")
                .put("mini", "minis")
                .put("outfit", "outfits")
                .put("tool", "skins").build();

        Gson gson = new GsonFireBuilder().createGsonBuilder().create();

        Map<String, TempImageMap> imageMaps = Maps.newLinkedHashMap();

        try (Reader reader = Files.newBufferedReader(contentPath)) {
            ContentData data = gson.fromJson(reader, ContentData.class);
            int iconWidth = data.iconWidth;
            int iconHeight = data.iconHeight;

            for (ImageInfo image : data.images) {
                TempImageMap map = new TempImageMap();
                map.name = image.name;
                map.image = image.image;
                imageMaps.put(map.name, map);
            }


            for (UnlockCategoryData itemCategory : data.items) {
                for (UnlockGroupData groupData : itemCategory.groups) {
                    for (UnlockData unlockData : groupData.content) {
                        if (unlockData.image != null) {
                            TempImageMap map = imageMaps.get(unlockData.image);
                            int index = (unlockData.yOffset / iconHeight) * 32 + (unlockData.xOffset / iconWidth);
                            // Get origin image
                            Path dataPath = Paths.get("cache", "api", dataMapping.get(itemCategory.id), unlockData.id + ".json");
                            if (Files.exists(dataPath)) {
                                try (BufferedReader itemReader = Files.newBufferedReader(dataPath)) {
                                    ItemData itemData = gson.fromJson(itemReader, ItemData.class);
                                    map.images.put(index, itemData.getIconName());
                                }
                            } else if (!map.images.containsKey(index)) {
                                map.images.put(index, unlockData.id + "-" + unlockData.id + ".png");
                            }
                        }
                    }
                }
            }
        }

        List<ImageMap> finalImageMaps = Lists.newArrayList();
        for (TempImageMap map : imageMaps.values()) {
            List<String> contents = Lists.newArrayList();
            for (int i = 0; i < map.images.size(); ++i) {
                contents.add(map.images.get(i));
            }
            finalImageMaps.add(new ImageMap(map.name, map.image, contents));
        }


        for (ImageMap map : finalImageMaps) {
            try (Writer writer = Files.newBufferedWriter(outPath.resolve(map.getName() + ".json"))) {
                gson.toJson(map, writer);
            }
        }


    }
    // Images
    private static class TempImageMap {
        public String name;
        public String image;
        public Map<Integer, String> images = Maps.newLinkedHashMap();
    }

    @Test
    public void deconstructMissingUnlocks() throws IOException {
        Path contentPath = Paths.get("input", "content.json");
        Path outRootPath = Paths.get("cache", "extract", "api");
        Path iconsPath = Paths.get("cache", "extract", "api", "icon");
        Path expectedRootPath = Paths.get("cache", "api");
        Files.createDirectories(iconsPath);

        Map<String, String> typeMapping = ImmutableMap.of("armor", "Armor", "weapon", "Weapon", "back", "Back");

        Gson gson = new GsonFireBuilder().createGsonBuilder().setPrettyPrinting().create();

        Table<String, Integer, IconDetails> iconLookup = HashBasedTable.create();
        for (Path imageMapFile : Files.newDirectoryStream(Paths.get("cache", "imagemaps"))) {
            try (Reader reader = Files.newBufferedReader(imageMapFile)) {
                ImageMap imageMap = gson.fromJson(reader, ImageMap.class);
                for (IconDetails iconDetails : imageMap) {
                    iconLookup.put(iconDetails.getImageId(), iconDetails.getXOffset() / 64 + 32 * (iconDetails.getYOffset() / 64),  iconDetails);
                }
            }
        }

        try (Reader reader = Files.newBufferedReader(contentPath)) {
            ContentData data = gson.fromJson(reader, ContentData.class);
            Map<String, String> imageMap = Maps.newHashMap();
            for (ImageInfo image : data.images) {
                imageMap.put(image.name, image.image);
            }

            for (UnlockCategoryData itemCategory : data.items) {
                String mappedId = typeToApiMapping.get(itemCategory.id);
                Path categoryOutPath = outRootPath.resolve(mappedId);
                Path expectedPath = expectedRootPath.resolve(mappedId);
                Files.createDirectories(categoryOutPath);
                for (UnlockGroupData groupData : itemCategory.groups) {
                    for (UnlockData unlock : groupData.content) {
                        if (Files.exists(expectedPath.resolve(unlock.id + ".json"))) {
                            continue;
                        }
                        ItemData itemData = new ItemData();
                        itemData.id = unlock.id;
                        itemData.name = unlock.name;
                        itemData.icon = "https://example.com/file/" + itemData.id + "/" + itemData.id + ".png";
                        itemData.rarity = unlock.rarity;
                        if (unlock.color != null) {
                            itemData.baseRGB = ColorUtil.hexToRgb(unlock.color);
                            itemData.cloth = new ColorData();
                            itemData.cloth.rgb = itemData.baseRGB;
                        }
                        itemData.chatLink = unlock.chatcode;
                        itemData.type = typeMapping.get(itemCategory.id);

                        if (unlock.image != null) {
                            IconDetails iconDetails = iconLookup.get(unlock.image, unlock.xOffset / 64 + 32 * (unlock.yOffset / 64));
                            if (iconDetails.getIconFile().length() > 20) {
                                itemData.icon = "https://render.guildwars2.com/file/" + iconDetails.getIconFile().replace("-", "/");
                            } else {
                                itemData.icon = "https://example.com/file/" + iconDetails.getIconFile().replace("-", "/");
                                Path imageMapPath = Paths.get("site", "img", imageMap.get(unlock.image));
                                Path outIconFile = iconsPath.resolve(itemData.id + "-" + itemData.id + ".png");
                                BufferedImage map = ImageIO.read(imageMapPath.toFile());
                                BufferedImage icon = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
                                Graphics2D graphics = icon.createGraphics();
                                graphics.drawImage(map, 0, 0, 64, 64, unlock.xOffset, unlock.yOffset, unlock.xOffset + 64, unlock.yOffset + 64, null);
                                ImageIO.write(icon, "png", outIconFile.toFile());
                                graphics.dispose();
                            }
                        }
                        try (Writer writer = Files.newBufferedWriter(categoryOutPath.resolve(itemData.id + ".json"))) {
                            gson.toJson(itemData, writer);
                        }
                    }
                }
            }
        }
    }

}
