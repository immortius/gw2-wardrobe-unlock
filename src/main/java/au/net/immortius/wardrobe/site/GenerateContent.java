package au.net.immortius.wardrobe.site;

import au.net.immortius.wardrobe.config.CategoryDefinitions;
import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.Grouping;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.Chatcode;
import au.net.immortius.wardrobe.gw2api.Emotes;
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
import com.google.gson.stream.MalformedJsonException;
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

    private static final String UNCLASSIFIED_GROUP_NAME = "New releases";
    private static final String GOLD = "gold";
    private static final String KARMA = "karma";
    private static final String TRADINGPOST = "tp";
    private static final String GUARANTEED_WARDROBE_UNLOCK = "gwu";
    private static final String BOUNTY = "bounty";
    private static final String CRAFT = "craft";
    private static final GenericType<Map<String, TradingPostEntry>> PRICE_MAP_TYPE = new GenericType<Map<String, TradingPostEntry>>() {
    };
    private static final GenericType<Map<String, Collection<String>>> UNLOCK_ITEM_MULTIMAP = new GenericType<Map<String, Collection<String>>>() {
    };
    private static final GenericType<List<VendorData>> VENDOR_DATA_LIST_TYPE = new GenericType<List<VendorData>>() {
    };
    private static final GenericType<Set<String>> STRING_SET_TYPE = new GenericType<Set<String>>() {
    };
    private static final Logger logger = LoggerFactory.getLogger(GenerateContent.class);
    private final Gson gson;
    private final Config config;
    private final Unlocks unlocks;
    private final Emotes emotes;

    public GenerateContent() throws IOException {
        this(Config.loadConfig());
    }

    public GenerateContent(Config config) {
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
        this.unlocks = new Unlocks(config, gson);
        this.emotes = new Emotes(config, gson);
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
        Set<String> craftableItems = loadCraftableItems();

        content.items = Lists.newArrayList();
        for (UnlockCategoryConfig unlockCategoryConfig : config.unlockCategories) {
            Map<String, Collection<String>> unlockItemMap;
            Map<String, String> itemUnlockMap;
            try (Reader unlockToSkinMappingReader = Files.newBufferedReader(config.paths.getUnlockItemsPath().resolve(unlockCategoryConfig.id + ".json"))) {
                unlockItemMap = gson.fromJson(unlockToSkinMappingReader, UNLOCK_ITEM_MULTIMAP.getType());
                itemUnlockMap = Maps.newHashMap();
                unlockItemMap.forEach((key, value) -> {
                    for (String item : value) {
                        itemUnlockMap.put(item, key);
                    }
                });
            }

            UnlockCategoryData itemCategory = new UnlockCategoryData();
            itemCategory.id = unlockCategoryConfig.id;
            itemCategory.name = unlockCategoryConfig.name;
            itemCategory.unlockUrl = unlockCategoryConfig.unlockUrl;

            Map<String, UnlockData> unlockDataMap = addUnlocks(unlockCategoryConfig, unlockItemMap, iconLookup, craftableItems);
            applyPrices(gson, unlockCategoryConfig, unlockDataMap);
            applyAcquisition(unlockCategoryConfig, unlockDataMap);
            applyGwu(unlockCategoryConfig, unlockDataMap);
            applyBounty(unlockCategoryConfig, unlockDataMap);
            applyVendors(unlockCategoryConfig, itemUnlockMap, unlockDataMap);
            categorizeUnlocks(unlockCategoryConfig, unlockDataMap, itemCategory);

            content.items.add(itemCategory);
        }

        try (Writer writer = Files.newBufferedWriter(config.paths.contentFile)) {
            gson.toJson(content, writer);
        }

    }

    private Set<String> loadCraftableItems() throws IOException {
        Set<String> craftableItems;
        try (Reader craftableItemsReader = Files.newBufferedReader(config.paths.getCraftableItemsFile())) {
            craftableItems = gson.fromJson(craftableItemsReader, STRING_SET_TYPE.getType());
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

    private void categorizeUnlocks(UnlockCategoryConfig unlockCategoryConfig, Map<String, UnlockData> unlockDataMap, UnlockCategoryData unlockCategoryData) throws IOException {

        Path groupsFile = config.paths.getGroupsPath().resolve(unlockCategoryConfig.id + ".json");

        List<UnlockCategoryGroupData> categories = Lists.newArrayList();
        List<UnlockGroupData> groups = Lists.newArrayList();
        if (Files.exists(groupsFile)) {
            try (Reader reader = Files.newBufferedReader(groupsFile)) {
                CategoryDefinitions categoryDefinitions = gson.fromJson(reader, CategoryDefinitions.class);
                for (Map.Entry<String, Map<String, Set<String>>> topLevelCategory : categoryDefinitions.getTopLevelCategories().entrySet()) {
                    UnlockCategoryGroupData categoryData = new UnlockCategoryGroupData();
                    categoryData.name = topLevelCategory.getKey();
                    for (Map.Entry<String, Set<String>> group : topLevelCategory.getValue().entrySet()) {
                        UnlockGroupData itemGroup = new UnlockGroupData();
                        itemGroup.groupName = group.getKey();
                        itemGroup.content = Lists.newArrayList();
                        for (String id : group.getValue()) {
                            UnlockData unlock = unlockDataMap.remove(id);
                            if (unlock != null) {
                                itemGroup.content.add(unlock);
                            } else if (id == null) {
                                logger.warn("Null item in group: {}", itemGroup.groupName);
                            } else {
                                logger.warn("Did not find {}:{}, possibly double-categorised", unlockCategoryConfig.id, id);
                            }
                        }
                        itemGroup.content.sort(Comparator.comparing((UnlockData a) -> a.rarity)
                                .thenComparing(a -> a.name)
                                .thenComparing(a -> a.id));
                        categoryData.groups.add(itemGroup);
                    }
                    categories.add(categoryData);
                }
                for (Map.Entry<String, Set<String>> grouping : categoryDefinitions.getDirectGroups().entrySet()) {
                    UnlockGroupData itemGroup = new UnlockGroupData();
                    itemGroup.groupName = grouping.getKey();
                    itemGroup.content = Lists.newArrayList();
                    for (String id : grouping.getValue()) {
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
            ListMultimap<String, String> suggestedGroups = ArrayListMultimap.create();
            unlockDataMap.forEach((key, value) -> {
                String[] words = value.name.split(" ");
                if (words.length > 1) {
                    suggestedGroups.put(words[0] + " " + words[1], key);
                }
                suggestedGroups.put(words[0], key);
            });
            for (String suggestedGroup : suggestedGroups.keySet()) {
                List<String> ids = suggestedGroups.get(suggestedGroup);
                if (ids.size() > 2) {
                    logger.info("Suggested group '{}': {}", suggestedGroup, ids);
                }
            }

            UnlockGroupData finalGroup = new UnlockGroupData();
            finalGroup.groupName = UNCLASSIFIED_GROUP_NAME;
            finalGroup.content = ImmutableList.copyOf(unlockDataMap.values());
            groups.add(finalGroup);
        }
        if (groups.size() > 0) {
            UnlockCategoryGroupData general = new UnlockCategoryGroupData();
            general.name = UNCLASSIFIED_GROUP_NAME;
            general.groups = groups;
            categories.add(general);
        }
        unlockCategoryData.categories = categories;
        unlockCategoryData.groups = new ArrayList<>();
    }

    private void applyVendors(UnlockCategoryConfig unlockCategoryConfig, Map<String, String> itemUnlockMap, Map<String, UnlockData> unlockDataMap) throws IOException {
        addVendorsToUnlocks(unlockCategoryConfig, itemUnlockMap, unlockDataMap);
        filterVendorsToCheapest(unlockDataMap);

        for (UnlockData unlock : unlockDataMap.values()) {
            if (!unlock.getVendors().isEmpty()) {
                unlock.sources.add("vendor");
                unlock.getVendors().forEach(vendorEntry -> unlock.sources.addAll(extractVendorSources(vendorEntry)));
            }
        }
    }

    private void filterVendorsToCheapest(Map<String, UnlockData> unlockDataMap) {
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

    private void addVendorsToUnlocks(UnlockCategoryConfig unlockCategoryConfig, Map<String, String> itemUnlockMap, Map<String, UnlockData> unlockDataMap) throws IOException {
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
                        String unlockId = itemUnlockMap.get(item.id);
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
        allUnsupportedCurrencies = allUnsupportedCurrencies.stream().filter(x -> !config.ignoreCurrencies.contains(x)).collect(Collectors.toSet());
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
        if (sources.size() > 1) {
            sources.remove(GOLD);
        }
        if (sources.size() > 1) {
            sources.remove(KARMA);
        }
        return sources;
    }

    private void applyGwu(UnlockCategoryConfig unlockCategoryConfig, Map<String, UnlockData> unlockDataMap) throws IOException {
        Path unlockFile = config.paths.getGuaranteedWardrobeUnlocksPath().resolve(unlockCategoryConfig.id + ".json");
        if (!Files.exists(unlockFile)) {
            return;
        }
        try (Reader gwuReader = Files.newBufferedReader(unlockFile)) {
            String[] contents = gson.fromJson(gwuReader, String[].class);
            for (String id : contents) {
                UnlockData unlock = unlockDataMap.get(id);
                if (unlock != null) {
                    unlock.sources.add(GUARANTEED_WARDROBE_UNLOCK);
                } else {
                    logger.warn("Unlock {} of {} not found, should be in guaranteed wardrobe unlock", id, unlockCategoryConfig.id);
                }
            }
        }
    }

    private void applyBounty(UnlockCategoryConfig unlockCategoryConfig, Map<String, UnlockData> unlockDataMap) throws IOException {
        Path unlockFile = config.paths.getBountyUnlocksPath().resolve(unlockCategoryConfig.id + ".json");
        if (!Files.exists(unlockFile)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(unlockFile)) {
            String[] contents = gson.fromJson(reader, String[].class);
            for (String id : contents) {
                UnlockData unlock = unlockDataMap.get(id);
                if (unlock != null) {
                    unlock.sources.add(BOUNTY);
                } else {
                    logger.warn("Unlock {} of {} not found, should be in bounty unlock", id, unlockCategoryConfig.id);
                }
            }
        }
    }

    private void applyAcquisition(UnlockCategoryConfig unlockCategoryConfig, Map<String, UnlockData> unlockDataMap) throws IOException {
        if (Files.exists(config.paths.baseInputPath.resolve(unlockCategoryConfig.id + "-acquisition"))) {
            try (DirectoryStream<Path> ds = Files.newDirectoryStream(config.paths.baseInputPath.resolve(unlockCategoryConfig.id + "-acquisition"))) {
                for (Path acquisitionMethodFile : ds) {
                    try (Reader methodReader = Files.newBufferedReader(acquisitionMethodFile)) {
                        Grouping acquisitionMethod = gson.fromJson(methodReader, Grouping.class);
                        for (String id : acquisitionMethod.contents) {
                            UnlockData unlockData = unlockDataMap.get(id);
                            if (unlockData != null) {
                                unlockData.sources.add(acquisitionMethod.name);
                            }
                        }
                    } catch (JsonSyntaxException | MalformedJsonException e) {
                        logger.error("Failed to process acquisition for {}", acquisitionMethodFile, e);
                    }
                }
            }
        }
    }

    private void applyPrices(Gson gson, UnlockCategoryConfig unlockCategoryConfig, Map<String, UnlockData> unlockDataMap) throws IOException {
        if (Files.exists(config.paths.getUnlockPricesPath().resolve(unlockCategoryConfig.id + ".json"))) {
            try (Reader priceLookupReader = Files.newBufferedReader(config.paths.getUnlockPricesPath().resolve(unlockCategoryConfig.id + ".json"))) {
                Map<String, TradingPostEntry> priceLookup = gson.fromJson(priceLookupReader, PRICE_MAP_TYPE.getType());
                for (Map.Entry<String, TradingPostEntry> entry : priceLookup.entrySet()) {
                    String itemId = entry.getKey();
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

    private Optional<ItemData> readItem(String id) {
        try (Reader itemReader = Files.newBufferedReader(config.paths.getItemPath().resolve(id + ".json"))) {
            return Optional.ofNullable(gson.fromJson(itemReader, ItemData.class));
        } catch (IOException ex) {
            return Optional.empty();
        }
    }

    private Map<String, UnlockData> addUnlocks(UnlockCategoryConfig unlockCategoryConfig, Map<String, Collection<String>> unlockItems, Map<String, IconDetails> iconLookup, Set<String> craftableItems) throws IOException {
        Map<String, UnlockData> result = Maps.newLinkedHashMap();
        if (unlockCategoryConfig.nonStandardId) {
            emotes.forEach(unlockCategoryConfig, (emoteData, itemData) -> {
                UnlockData unlock = new UnlockData();
                result.put(emoteData.id, unlock);
                unlock.id = emoteData.id;
                unlock.name = emoteData.id.substring(0, 1).toUpperCase(Locale.ROOT) + emoteData.id.substring(1);
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
        } else {
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
        }
        return result;
    }

    private boolean isCraftable(String id, Map<String, Collection<String>> unlockItems, Set<String> craftableItems) {
        if (unlockItems.containsKey(id)) {
            for (String itemId : unlockItems.get(id)) {
                if (craftableItems.contains(itemId)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Rarity determineRarity(String id, ItemData unlock, Map<String, Collection<String>> unlockItems) {
        if (unlockItems.containsKey(id)) {
            Rarity rarity = null;
            for (String itemId : unlockItems.get(id)) {
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

    private Optional<Rarity> getRarityFromItem(String item) {
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

    private Optional<String> determineChatcode(ItemData itemData, UnlockCategoryConfig unlockCategoryConfig, Map<String, Collection<String>> unlockItems) {
        if (unlockCategoryConfig.chatcodeType == 0) {
            return Optional.empty();
        }
        if (!Strings.isNullOrEmpty(itemData.chatLink)) {
            return Optional.of(itemData.chatLink);
        }
        if (unlockCategoryConfig.useItemForChatcode) {
            String itemId = null;
            Optional<String> id = itemData.getUnlockItems().stream().findFirst();
            if (id.isPresent()) {
                itemId = id.get();
            }
            if (itemId == null && unlockItems.containsKey(itemData.id)) {
                itemId = unlockItems.get(itemData.id).stream().findFirst().get();
            }
            if (itemId != null) {
                return Optional.of(Chatcode.create(unlockCategoryConfig.chatcodeType, itemId));
            }
        } else {
            return Optional.of(Chatcode.create(unlockCategoryConfig.chatcodeType, itemData.id));
        }
        return Optional.empty();
    }

}
