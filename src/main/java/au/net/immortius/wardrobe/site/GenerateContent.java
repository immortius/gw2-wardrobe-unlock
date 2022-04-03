package au.net.immortius.wardrobe.site;

import au.net.immortius.wardrobe.config.*;
import au.net.immortius.wardrobe.gw2api.Chatcode;
import au.net.immortius.wardrobe.gw2api.Rarity;
import au.net.immortius.wardrobe.gw2api.Unlocks;
import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import au.net.immortius.wardrobe.imagemap.IconDetails;
import au.net.immortius.wardrobe.imagemap.ImageMap;
import au.net.immortius.wardrobe.site.entities.*;
import au.net.immortius.wardrobe.util.ColorUtil;
import au.net.immortius.wardrobe.vendors.entities.VendorData;
import au.net.immortius.wardrobe.vendors.entities.VendorItem;
import com.google.common.base.Strings;
import com.google.common.collect.*;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.GenericType;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates the content.json file that drives the site
 */
public class GenerateContent {

    private static final String GENERAL_GROUP_NAME = "General";
    private static final String UNCLASSIFIED_GROUP_NAME = "New";
    private static final String GOLD = "gold";
    private static final String KARMA = "karma";
    private static final String TRADINGPOST = "tp";
    private static final String GUARANTEED_WARDROBE_UNLOCK = "gwu";
    private static final String CRAFT = "craft";
    private static final GenericType<Map<Integer, TradingPostEntry>> PRICE_MAP_TYPE = new GenericType<Map<Integer, TradingPostEntry>>() {
    };
    private static final GenericType<Map<Integer, Collection<Integer>>> UNLOCK_ITEM_MULTIMAP = new GenericType<Map<Integer, Collection<Integer>>>() {
    };
    private static final GenericType<List<VendorData>> VENDOR_DATA_LIST_TYPE = new GenericType<List<VendorData>>() {
    };
    private static final GenericType<Set<Integer>> INTEGER_SET_TYPE = new GenericType<Set<Integer>>() {
    };
    private static Logger logger = LoggerFactory.getLogger(GenerateContent.class);
    private Gson gson;
    private Config config;
    private Unlocks unlocks;

    public GenerateContent() throws IOException {
        this(Config.loadConfig());
    }

    public GenerateContent(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
        this.unlocks = new Unlocks(config, gson);
    }


    public static void main(String... args) throws Exception {
        new GenerateContent().run();
    }

    public void run() throws IOException {
        logger.info("Generating content.json");
        ContentData content = new ContentData();
        content.iconHeight = config.imageMapper.iconSize;
        content.iconWidth = config.imageMapper.iconSize;
        content.images = Lists.newArrayList();

        Map<String, IconDetails> iconLookup = processImageMaps(content);
        Set<Integer> craftableItems = loadCraftableItems();

        content.items = Lists.newArrayList();
        for (UnlockCategoryConfig unlockCategoryConfig : config.unlockCategories) {
            Map<Integer, Collection<Integer>> unlockItemMap;
            Map<Integer, Integer> itemUnlockMap;
            try (Reader unlockToSkinMappingReader = Files.newBufferedReader(config.paths.getUnlockItemsPath().resolve(unlockCategoryConfig.id + ".json"))) {
                unlockItemMap = gson.fromJson(unlockToSkinMappingReader, UNLOCK_ITEM_MULTIMAP.getType());
                itemUnlockMap = Maps.newHashMap();
                unlockItemMap.forEach((key, value) -> {
                    for (int item : value) {
                        itemUnlockMap.put(item, key);
                    }
                });
            }

            UnlockCategoryData itemCategory = new UnlockCategoryData();
            itemCategory.id = unlockCategoryConfig.id;
            itemCategory.name = unlockCategoryConfig.name;
            itemCategory.unlockUrl = unlockCategoryConfig.unlockUrl;

            Map<Integer, UnlockData> unlockDataMap = addUnlocks(unlockCategoryConfig, unlockItemMap, iconLookup, craftableItems);
            applyPrices(gson, unlockCategoryConfig, unlockDataMap);
            applyAcquisition(unlockCategoryConfig, unlockDataMap);
            applyGwu(unlockCategoryConfig, unlockDataMap);
            applyVendors(unlockCategoryConfig, itemUnlockMap, unlockDataMap);
            categorizeUnlocks(unlockCategoryConfig, unlockDataMap, itemCategory);

            content.items.add(itemCategory);
        }

        try (Writer writer = Files.newBufferedWriter(config.paths.contentFile)) {
            gson.toJson(content, writer);
        }

    }

    private Set<Integer> loadCraftableItems() throws IOException {
        Set<Integer> craftableItems;
        try (Reader craftableItemsReader = Files.newBufferedReader(config.paths.getCraftableItemsFile())) {
            craftableItems = gson.fromJson(craftableItemsReader, INTEGER_SET_TYPE.getType());
        }
        return craftableItems;
    }

    private Map<String, IconDetails> processImageMaps(ContentData content) throws IOException {
        Map<String, IconDetails> iconLookup = Maps.newHashMap();
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(config.paths.getAtlasPath())) {
            for (Path imageMapFile : ds) {
                try (Reader reader = Files.newBufferedReader(imageMapFile)) {
                    ImageMap imageMap = gson.fromJson(reader, ImageMap.class);
                    ImageInfo imageInfo = new ImageInfo();
                    imageInfo.name = imageMap.getName();
                    imageInfo.image = imageMap.getImage();
                    content.images.add(imageInfo);
                    for (IconDetails iconDetails : imageMap) {
                        iconLookup.put(iconDetails.getIconFile(), iconDetails);
                    }
                }
            }
            return iconLookup;
        }
    }

    private void categorizeUnlocks(UnlockCategoryConfig unlockCategoryConfig, Map<Integer, UnlockData> unlockDataMap, UnlockCategoryData unlockCategoryData) throws IOException {

        Path groupsFile = config.paths.getGroupsPath().resolve(unlockCategoryConfig.id + ".json");

        List<UnlockCategoryGroupData> categories = Lists.newArrayList();
        List<UnlockGroupData> groups = Lists.newArrayList();
        if (Files.exists(groupsFile)) {
            try (Reader reader = Files.newBufferedReader(groupsFile)) {
                CategoryDefinitions categoryDefinitions = gson.fromJson(reader, CategoryDefinitions.class);
                for (Map.Entry<String, Map<String, Set<Integer>>> topLevelCategory : categoryDefinitions.getTopLevelCategories().entrySet()) {
                    UnlockCategoryGroupData categoryData = new UnlockCategoryGroupData();
                    categoryData.name = topLevelCategory.getKey();
                    for (Map.Entry<String, Set<Integer>> group : topLevelCategory.getValue().entrySet()) {
                        UnlockGroupData itemGroup = new UnlockGroupData();
                        itemGroup.groupName = group.getKey();
                        itemGroup.content = Lists.newArrayList();
                        for (Integer id : group.getValue()) {
                            UnlockData unlock = unlockDataMap.remove(id);
                            if (unlock != null) {
                                itemGroup.content.add(unlock);
                            }
                        }
                        itemGroup.content.sort(Comparator.comparing((UnlockData a) -> a.rarity)
                                .thenComparing(a -> a.name)
                                .thenComparing(a -> a.id));
                        categoryData.groups.add(itemGroup);
                    }
                    categories.add(categoryData);
                }
                for (Map.Entry<String, Set<Integer>> grouping : categoryDefinitions.getDirectGroups().entrySet()) {
                    UnlockGroupData itemGroup = new UnlockGroupData();
                    itemGroup.groupName = grouping.getKey();
                    itemGroup.content = Lists.newArrayList();
                    for (Integer id : grouping.getValue()) {
                        UnlockData unlock = unlockDataMap.remove(id);
                        if (unlock != null) {
                            itemGroup.content.add(unlock);
                        }
                    }
                    itemGroup.content.sort(Comparator.comparing((UnlockData a) -> a.rarity)
                            .thenComparing(a -> a.name)
                            .thenComparing(a -> a.id));
                    groups.add(itemGroup);
                }
            } catch (JsonSyntaxException e) {
                logger.error("Failed to read {}", groupsFile, e);
            }
        }

//        groups.sort((o1, o2) -> {
//            if (GENERAL_GROUP_NAME.equals(o1.groupName)) {
//                return 1;
//            } else if (GENERAL_GROUP_NAME.equals(o2.groupName)) {
//                return -1;
//            } else {
//                return o1.groupName.compareTo(o2.groupName);
//            }
//        });

        // Residual group
        if (!unlockDataMap.isEmpty()) {
            ListMultimap<String, Integer> suggestedGroups = ArrayListMultimap.create();
            unlockDataMap.forEach((key, value) -> {
                String[] words = value.name.split(" ");
                if (words.length > 1) {
                    suggestedGroups.put(words[0] + " " + words[1], key);
                }
                suggestedGroups.put(words[0], key);
            });
            for (String suggestedGroup : suggestedGroups.keySet()) {
                List<Integer> ids = suggestedGroups.get(suggestedGroup);
                if (ids.size() > 2) {
                    logger.info("Suggested group '{}': {}", suggestedGroup, ids);
                }
            }


            UnlockGroupData finalGroup = new UnlockGroupData();
            finalGroup.groupName = UNCLASSIFIED_GROUP_NAME;
            finalGroup.content = ImmutableList.copyOf(unlockDataMap.values());
            groups.add(finalGroup);
        }
        UnlockCategoryGroupData general = new UnlockCategoryGroupData();
        general.name = "General";
        general.groups = groups;
        categories.add(general);
        unlockCategoryData.categories = categories;
        unlockCategoryData.groups = new ArrayList<>();
    }

    private void applyVendors(UnlockCategoryConfig unlockCategoryConfig, Map<Integer, Integer> itemUnlockMap, Map<Integer, UnlockData> unlockDataMap) throws IOException {
        addVendorsToUnlocks(unlockCategoryConfig, itemUnlockMap, unlockDataMap);
        filterVendorsToCheapest(unlockDataMap);

        for (UnlockData unlock : unlockDataMap.values()) {
            if (!unlock.getVendors().isEmpty()) {
                unlock.sources.add("vendor");
                unlock.getVendors().forEach(vendorEntry -> unlock.sources.addAll(extractVendorSources(vendorEntry)));
            }
        }
    }

    private void filterVendorsToCheapest(Map<Integer, UnlockData> unlockDataMap) {
        for (UnlockData unlock : unlockDataMap.values()) {
            ListMultimap<String, VendorInfo> vendorsByCostType = ArrayListMultimap.create();
            List<VendorInfo> finalVendors = Lists.newArrayList();
            for (VendorInfo vendor : unlock.getVendors()) {
                if (vendor.cost.size() > 1) {
                    finalVendors.add(vendor);
                } else {
                    vendorsByCostType.put(vendor.cost.get(0).type, vendor);
                }
            }

            for (Collection<VendorInfo> vendors : vendorsByCostType.asMap().values()) {
                int minCost = Integer.MAX_VALUE;
                List<VendorInfo> cheapest = Lists.newArrayList();
                for (VendorInfo vendor : vendors) {
                    int cost = vendor.cost.get(0).value;
                    if (cost < minCost) {
                        cheapest.clear();
                        cheapest.add(vendor);
                        minCost = vendor.cost.get(0).value;
                    } else if (cost == minCost) {
                        cheapest.add(vendor);
                    }
                }
                finalVendors.addAll(cheapest);
            }
            unlock.getVendors().clear();
            unlock.getVendors().addAll(finalVendors);
        }
    }

    private void addVendorsToUnlocks(UnlockCategoryConfig unlockCategoryConfig, Map<Integer, Integer> itemUnlockMap, Map<Integer, UnlockData> unlockDataMap) throws IOException {
        Set<String> allUnsupportedCurrencies = Sets.newLinkedHashSet();
        Path vendorFile = config.paths.getVendorsPath().resolve(unlockCategoryConfig.id + ".json");
        if (!Files.exists(vendorFile)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(vendorFile)) {
            List<VendorData> vendors = gson.fromJson(reader, VENDOR_DATA_LIST_TYPE.getType());
            for (VendorData vendor : vendors) {
                for (VendorItem item : vendor.items) {
                    UnlockData unlock = null;
                    if (unlockCategoryConfig.useItemForVendor) {
                        Integer unlockId = itemUnlockMap.get(item.id);
                        if (unlockId != null) {
                            unlock = unlockDataMap.get(unlockId);
                        }
                    } else {
                        unlock = unlockDataMap.get(item.id);
                    }
                    if (unlock != null) {
                        List<String> unsupportedCurrencies = item.cost.stream().map(x -> x.type).filter(x -> !config.supportedCurrencies.contains(x)).collect(Collectors.toList());
                        if (unsupportedCurrencies.isEmpty()) {
                            VendorInfo vendorEntry = new VendorInfo();
                            vendorEntry.vendorName = vendor.name;
                            vendorEntry.vendorUrl = vendor.url;
                            vendorEntry.cost = item.cost;
                            if (!unlock.getVendors().contains(vendorEntry)) {
                                unlock.getVendors().add(vendorEntry);
                            }
                        } else {
                            allUnsupportedCurrencies.addAll(unsupportedCurrencies);
                        }
                    }
                }
            }
        }
        if (!allUnsupportedCurrencies.isEmpty()) {
            logger.warn("Unsupported currencies detected for {}: {}", unlockCategoryConfig.id, allUnsupportedCurrencies);
        }
    }

    private Set<String> extractVendorSources(VendorInfo vendorEntry) {
        Set<String> sources = Sets.newLinkedHashSet();
        for (CostComponent costComponent : vendorEntry.cost) {
            sources.add(costComponent.type);
        }
        // If there is a cost component besides gold or karma, then
        // remove gold/karma from the sources - we want to emphasize
        // the primary component of the cost and not muddle things
        if (sources.size() > 1 && sources.contains(GOLD)) {
            sources.remove(GOLD);
        }
        if (sources.size() > 1 && sources.contains(KARMA)) {
            sources.remove(KARMA);
        }
        return sources;
    }

    private void applyGwu(UnlockCategoryConfig unlockCategoryConfig, Map<Integer, UnlockData> unlockDataMap) throws IOException {
        Path unlockFile = config.paths.getGuaranteedWardrobeUnlocksPath().resolve(unlockCategoryConfig.id + ".json");
        if (!Files.exists(unlockFile)) {
            return;
        }
        try (Reader gwuReader = Files.newBufferedReader(unlockFile)) {
            int[] contents = gson.fromJson(gwuReader, int[].class);
            for (int id : contents) {
                UnlockData unlock = unlockDataMap.get(id);
                if (unlock != null) {
                    unlock.sources.add(GUARANTEED_WARDROBE_UNLOCK);
                } else {
                    logger.warn("Unlock {} of {} not found, should be in guaranteed wardrobe unlock", id, unlockCategoryConfig.id);
                }
            }
        }
    }

    private void applyAcquisition(UnlockCategoryConfig unlockCategoryConfig, Map<Integer, UnlockData> unlockDataMap) throws IOException {
        if (Files.exists(config.paths.baseInputPath.resolve(unlockCategoryConfig.id + "-acquisition"))) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(config.paths.baseInputPath.resolve(unlockCategoryConfig.id + "-acquisition"))) {
                for (Path acquisitionMethodFile : ds) {
                    try (Reader methodReader = Files.newBufferedReader(acquisitionMethodFile)) {
                        Grouping acquisitionMethod = gson.fromJson(methodReader, Grouping.class);
                        for (int id : acquisitionMethod.contents) {
                            UnlockData unlockData = unlockDataMap.get(id);
                            if (unlockData != null) {
                                unlockData.sources.add(acquisitionMethod.name);
                            }
                        }
                    }
                }
            }
        }
    }

    private void applyPrices(Gson gson, UnlockCategoryConfig unlockCategoryConfig, Map<Integer, UnlockData> unlockDataMap) throws IOException {
        if (Files.exists(config.paths.getUnlockPricesPath().resolve(unlockCategoryConfig.id + ".json"))) {
            try (Reader priceLookupReader = Files.newBufferedReader(config.paths.getUnlockPricesPath().resolve(unlockCategoryConfig.id + ".json"))) {
                Map<Integer, TradingPostEntry> priceLookup = gson.fromJson(priceLookupReader, PRICE_MAP_TYPE.getType());
                for (Map.Entry<Integer, TradingPostEntry> entry : priceLookup.entrySet()) {
                    Integer itemId = entry.getKey();
                    UnlockData unlockData = unlockDataMap.get(itemId);
                    if (unlockData == null) {
                        logger.error("Found price for missing unlock {} of type {}", itemId, unlockCategoryConfig.id);
                    } else {
                        TradingPostEntry tpEntry = entry.getValue();
                        PriceEntry bestBuyPrice = tpEntry.getBestBuyPrice();
                        readItem(bestBuyPrice.getItemId())
                                .ifPresent(i -> bestBuyPrice.setItemName(i.getName()));
                        PriceEntry bestSellPrice = tpEntry.getBestSellPrice();
                        readItem(bestSellPrice.getItemId())
                                .ifPresent(i -> bestSellPrice.setItemName(i.getName()));
                        unlockData.priceData = tpEntry;
                        unlockData.sources.add(GOLD);
                        unlockData.sources.add(TRADINGPOST);
                    }

                }
            }
        }
    }

    private Optional<ItemData> readItem(Integer id) {
        try (Reader itemReader = Files.newBufferedReader(config.paths.getItemPath().resolve(id + ".json"))) {
            return Optional.ofNullable(gson.fromJson(itemReader, ItemData.class));
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    private Map<Integer, UnlockData> addUnlocks(UnlockCategoryConfig unlockCategoryConfig, Map<Integer, Collection<Integer>> unlockItems, Map<String, IconDetails> iconLookup, Set<Integer> craftableItems) throws IOException {
        Map<Integer, UnlockData> result = Maps.newLinkedHashMap();
        unlocks.forEach(unlockCategoryConfig, itemData -> {
            UnlockData unlock = new UnlockData();
            result.put(itemData.id, unlock);
            unlock.id = itemData.id;
            unlock.name = itemData.getName();
            unlock.sources = Sets.newLinkedHashSet();
            unlock.rarity = determineRarity(itemData.id, itemData, unlockItems);
            if (isCraftable(itemData.id, unlockItems, craftableItems)) {
                unlock.sources.add(CRAFT);
            }
            if (itemData.baseRGB != null) {
                unlock.color = ColorUtil.rgbToHex(itemData.cloth.rgb);
            } else if (itemData.icon != null) {
                IconDetails iconDetails = iconLookup.get(itemData.getIconName());
                if (iconDetails != null) {
                    unlock.xOffset = iconDetails.getXOffset();
                    unlock.yOffset = iconDetails.getYOffset();
                    unlock.image = iconDetails.getImageId();
                } else {
                    logger.warn("Unable to resolve icon {} for {} ({}) - excluding", itemData.icon, itemData.getName(), itemData.id);
                    result.remove(itemData.id);
                }
            }
            determineChatcode(itemData, unlockCategoryConfig, unlockItems).ifPresent(x -> unlock.chatcode = x);
        });
        return result;
    }

    private boolean isCraftable(int id, Map<Integer, Collection<Integer>> unlockItems, Set<Integer> craftableItems) {
        if (unlockItems.containsKey(id)) {
            for (int itemId : unlockItems.get(id)) {
                if (craftableItems.contains(itemId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Rarity determineRarity(int id, ItemData unlock, Map<Integer, Collection<Integer>> unlockItems) {
        if (unlockItems.containsKey(id)) {
            Rarity rarity = null;
            for (int itemId : unlockItems.get(id)) {
                Optional<Rarity> rarityFromItem = getRarityFromItem(itemId);
                if (rarityFromItem.isPresent()) {
                    Rarity newRarity = rarityFromItem.get();
                    if (rarity == null || rarity.compareTo(newRarity) > 0) {
                        rarity = newRarity;
                    }
                }
            }
            if (rarity != null) {
                return rarity;
            }
        }
        if (unlock.rarity != null) {
            return unlock.rarity;
        }

        return Rarity.Basic;
    }

    private Optional<Rarity> getRarityFromItem(int item) {
        Path itemPath = config.paths.getItemPath().resolve(item + ".json");
        if (!Files.exists(itemPath)) {
            return Optional.empty();
        }
        try (Reader itemReader = Files.newBufferedReader(itemPath)) {
            ItemData itemData = gson.fromJson(itemReader, ItemData.class);
            return Optional.ofNullable(itemData.rarity);
        } catch (IOException e) {
            logger.error("Failed to read item {}", item, e);
            return Optional.empty();
        }

    }

    private Optional<String> determineChatcode(ItemData itemData, UnlockCategoryConfig unlockCategoryConfig, Map<Integer, Collection<Integer>> unlockItems) {
        if (unlockCategoryConfig.chatcodeType == 0) {
            return Optional.empty();
        }
        if (!Strings.isNullOrEmpty(itemData.chatLink)) {
            return Optional.of(itemData.chatLink);
        }
        if (unlockCategoryConfig.useItemForChatcode) {
            int itemId = 0;
            Optional<Integer> id = itemData.getUnlockItems().stream().findFirst();
            if (id.isPresent()) {
                itemId = id.get();
            }
            if (itemId == 0 && unlockItems.containsKey(itemData.id)) {
                itemId = unlockItems.get(itemData.id).stream().findFirst().get();
            }
            if (itemId != 0) {
                return Optional.of(Chatcode.create(unlockCategoryConfig.chatcodeType, itemId));
            }
        } else {
            return Optional.of(Chatcode.create(unlockCategoryConfig.chatcodeType, itemData.id));
        }
        return Optional.empty();
    }

}
