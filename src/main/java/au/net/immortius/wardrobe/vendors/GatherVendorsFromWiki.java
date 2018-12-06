package au.net.immortius.wardrobe.vendors;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.config.UnlockCategoryConfig;
import au.net.immortius.wardrobe.gw2api.Items;
import au.net.immortius.wardrobe.gw2api.Skins;
import au.net.immortius.wardrobe.site.entities.CostComponent;
import au.net.immortius.wardrobe.util.REST;
import au.net.immortius.wardrobe.vendors.entities.VendorData;
import au.net.immortius.wardrobe.vendors.entities.VendorItem;
import com.google.common.collect.*;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A crawler for the GW2 wiki to discover and map out vendors and the unlocks they provide
 */
public class GatherVendorsFromWiki {
    /**
     * The service type for Armorsmith. Only care about this because the armorsmith pages are poor quality, so want
     * to skip them for error logging
     */
    private static final String ARMORSMITH = "Armorsmith";
    private static Logger logger = LoggerFactory.getLogger(GatherVendorsFromWiki.class);

    private final Gson gson;
    private final Config config;
    private final Client client;
    private Set<WikiUrl> processed = Sets.newHashSet();
    private ListMultimap<String, VendorData> vendorData = ArrayListMultimap.create();
    private final Skins skins;
    private final Items items;

    private Pattern valueMatcher = Pattern.compile("(.+?) (\\d{8})");

    public GatherVendorsFromWiki() throws IOException {
        this(Config.loadConfig());
    }

    public GatherVendorsFromWiki(Config config) {
        this.config = config;
        this.client = ClientBuilder.newClient();
        this.gson = new GsonFireBuilder().createGson();
        this.skins = new Skins(config, gson);
        this.items = new Items(config, gson);
    }

    public static void main(String... args) throws Exception {
        new GatherVendorsFromWiki().run();
    }

    public void run() throws IOException {
        Files.createDirectories(config.paths.getWikiCachePath());
        for (PageType value : PageType.values()) {
            Files.createDirectories(config.paths.getWikiCachePath().resolve(value.getPath()));
        }
        Files.createDirectories(config.paths.getWikiCachePath());

        for (String categoryPage : config.vendorCrawler.getCategoryPages()) {
            scanCategory(new WikiUrl(categoryPage));
        }
        for (String vendorPage : config.vendorCrawler.getVendorPages()) {
            scanVendorPage(new WikiUrl(vendorPage));
        }

        for (UnlockCategoryConfig unlockCategory : config.unlockCategories) {
            Path saveToPath = config.paths.getVendorsPath().resolve(unlockCategory.id + ".json");
            try (BufferedWriter writer = Files.newBufferedWriter(saveToPath)) {
                gson.toJson(vendorData.get(unlockCategory.id), writer);
            }
        }

    }

    private void scanCategory(WikiUrl category) throws IOException {
        if (category.isInvalid() || !processed.add(category)) {
            return;
        }

        Document categoryDoc = Jsoup.parse(getPage(PageType.CATEGORY, category));
        List<Element> pageLinks = categoryDoc.select("#mw-pages a").stream().filter(x -> !x.attr("href").contains("Category:")).collect(Collectors.toList());
        for (Element pageLink : pageLinks) {
            scanVendorPage(new WikiUrl(pageLink.attr("href")));
        }
        if (pageLinks.size() >= 200) {
            scanCategory(new WikiUrl(category.getBaseUrl(), pageLinks.get(pageLinks.size() - 1).attr("title")));
        }

        Elements subcategoryLinks = categoryDoc.select("#mw-subcategories a");
        for (Element subcategoryLink : subcategoryLinks) {
            scanCategory(new WikiUrl(subcategoryLink.attr("href")));
        }
    }

    private void scanVendorPage(WikiUrl url) throws IOException {
        if (url.isInvalid() || !processed.add(url) || config.vendorCrawler.getIgnorePages().contains(url.getBaseUrl())) {
            return;
        }

        Document vendorDoc = Jsoup.parse(getPage(PageType.VENDOR, url));
        ListMultimap<String, VendorItem> vendorItems = ArrayListMultimap.create();

        String vendorType = "";
        Elements services = vendorDoc.select("dt:matches(Services)");
        if (!services.isEmpty()) {
            vendorType = services.next().text();
        }

        Elements tables = vendorDoc.select("table.npc");
        for (Element table : tables) {
            Elements headers = table.select("th");
            int itemColumn = -1;
            int typeColumn = -1;
            int costColumn = -1;
            for (int i = 0; i < headers.size(); ++i) {
                Element header = headers.get(i);
                switch (header.text().trim()) {
                    case "Item":
                        itemColumn = i;
                        break;
                    case "Type":
                        typeColumn = i;
                        break;
                    case "Cost":
                    case "Price":
                        costColumn = i;
                        break;
                }
            }
            if (itemColumn == -1 || costColumn == -1) {
                logger.warn("Missing columns on url {}", url, table);
                continue;
            }

            Elements rows = table.select("tbody tr");
            for (Element row : rows) {
                if (!row.select("th").isEmpty()) {
                    continue;
                }

                List<CostComponent> cost = extractCost(row.child(costColumn));
                if (cost.isEmpty()) {
                    // Small check for armorsmith directly, just because a lot of armorsmith pages are broken
                    if (!vendorType.equals(ARMORSMITH)) {
                        logger.warn("Empty Price for: {} on {}", row.child(itemColumn).text(), url);
                    }
                    continue;
                }


                String type = "";
                if (typeColumn != -1) {
                    type = row.child(typeColumn).text();
                }
                WikiUrl itemUrl = new WikiUrl(row.child(itemColumn).select("a").attr("href"));
                if (type.isEmpty() || config.vendorCrawler.skinTypes.contains(type)) {
                    for (Integer skinId : getSkinId(itemUrl)) {
                        VendorItem vendorItem = new VendorItem();
                        vendorItem.cost = cost;
                        vendorItem.id = skinId;
                        skins.getSkinType(skinId).ifPresent(skinType -> {
                            vendorItems.put(skinType, vendorItem);
                        });
                    }
                }
                if (type.isEmpty() || config.vendorCrawler.getMiniatureTypes().contains(type)) {
                    for (int miniId : getMiniId(itemUrl)) {
                        VendorItem vendorItem = new VendorItem();
                        vendorItem.cost = cost;
                        vendorItem.id = miniId;
                        vendorItems.put(config.vendorCrawler.miniId, vendorItem);
                    }
                }
            }
        }
        for (String category : vendorItems.keySet()) {
            VendorData vendor = new VendorData();
            vendor.name = url.getDisplayName();
            vendor.items = vendorItems.get(category);
            vendor.url = config.vendorCrawler.rootUrl + url.getBaseUrl();
            vendorData.put(category, vendor);
        }
    }

    private Set<Integer> getMiniId(WikiUrl itemUrl) throws IOException {
        Set<Integer> result = Sets.newLinkedHashSet();
        Document doc = Jsoup.parse(getPage(PageType.ITEM, itemUrl));
        Elements miniItemIds = doc.select("span.gamelink[data-type='item']");
        if (!miniItemIds.isEmpty()) {
            int itemId = Integer.parseInt(miniItemIds.attr("data-id"));
            items.get(itemId).ifPresent(item -> {
                if (item.details != null && item.details.minipetId != null) {
                    result.add(item.details.minipetId);
                }
            });
        }
        return result;
    }

    private Set<Integer> getSkinId(WikiUrl itemUrl) throws IOException {
        if (itemUrl.isInvalid()) {
            return Collections.emptySet();
        }
        Set<Integer> result = Sets.newLinkedHashSet();
        Document doc = Jsoup.parse(getPage(PageType.ITEM, itemUrl));

        Elements skinIds = doc.select("span.gamelink[data-type='skin']");
        if (!skinIds.isEmpty()) {
            result.add(Integer.parseInt(skinIds.attr("data-id")));
        }
        Elements skinsTitle = doc.select("dt:matches(Skin)");
        if (!skinsTitle.isEmpty()) {
            for (Element skinLink : skinsTitle.next().select("a")) {
                result.addAll(getSkinId(new WikiUrl(skinLink.attr("href"))));
            }
        }
        Elements itemType = doc.select("dt:matches(Item type)");
        if (!itemType.isEmpty() && itemType.next().text().equals("Container")) {
            result.addAll(getContainerSkins(doc));
        }

        return result;
    }

    private Collection<Integer> getContainerSkins(Document doc) throws IOException {
        Set<Integer> result = Sets.newLinkedHashSet();
        Elements contentsHeading = doc.select("h2:matches(Contents)");
        if (!contentsHeading.isEmpty()) {
            for (Element itemLink : contentsHeading.next().select("span ~ a")) {
                result.addAll(getSkinId(new WikiUrl(itemLink.attr("href"))));
            }
        }
        return result;
    }

    private List<CostComponent> extractCost(Element cost) {
        List<CostComponent> result = Lists.newArrayList();
        if (cost.hasAttr("data-sort-value")) {
            String costString = cost.attr("data-sort-value");
            Matcher matches = valueMatcher.matcher(costString);
            while (matches.find()) {
                result.add(new CostComponent(convertCurrency(matches.group(1)), Integer.parseInt(matches.group(2), 10)));
            }
        } else {
            Elements childDivs = cost.select("div");
            if (childDivs.isEmpty()) {
                extractCostComponent(cost).ifPresent(result::add);
            } else {
                for (Element component : childDivs) {
                    extractCostComponent(component).ifPresent(result::add);
                }
            }
        }
        return result;
    }

    private Optional<CostComponent> extractCostComponent(Element cost) {
        try {
            String type = convertCurrency(cost.select("a").attr("title"));
            String rawCost = cost.text().trim().split("&nbsp")[0].replace(",", "");
            int quantity = Integer.parseInt(rawCost);
            return Optional.of(new CostComponent(type, quantity));
        } catch (NumberFormatException e) {
            logger.error("Not a valid cost string? \"{}\" of {}", cost.text(), cost.parent().text());
            return Optional.empty();
        }
    }

    private Map<String, String> currencyMap = ImmutableMap.<String, String>builder()
            .put("badgeofhonor", "boh")
            .put("blacklionstatuette", "bls")
            .put("coin", "gold")
            .put("copper", "gold")
            .put("coppercoin", "gold")
            .put("ascaloniantears", "ascaloniantear")
            .put("deadlyblooms", "deadlybloom")
            .put("flamelegioncharrcarving", "charrcarving")
            .put("flamelegioncharrcarvings", "charrcarving")
            .put("freshwinterberry", "winterberries")
            .put("baublebubble", "bauble")
            .put("difluoritecrystal","difluorite")
            .put("gaetingcrystal", "gaeting")
            .put("integratedfractalmatrix", "integratedmatrix")
            .put("lumpofaurillium","aurillium")
            .put("lumpofmistonium", "mistonium")
            .put("wvwskirmishclaimticket", "wvwsct")
            .put("tradecontracts", "tradecontract")
            .put("blacklionclaimticket", "blt")
            .put("manifestoofthemoletariate","manifesto")
            .put("reclaimedmetalplate","reclaimedplate")
            .put("grandmasterartificersmark", "grandmasterartifactmark")
            .put("grandmasterhuntsmansmark","grandmasterhuntsmanmark")
            .put("grandmasterweaponsmithsmark", "grandmasterweaponmark")
            .put("knowledgecrystals", "knowledgecrystal")
            .put("manifestosofthemoletariate", "manifesto")
            .put("sealsofbeetletun", "sealofbeetletun")
            .put("shardsofzhaitan", "shardofzhaitan")
            .put("symbolsofkoda", "symbolofkoda")
            .build();


    private String convertCurrency(String currency) {
        String rawCurrency = currency.replaceAll(" ", "").replaceAll("'", "").toLowerCase(Locale.ENGLISH);
        if (currencyMap.containsKey(rawCurrency)) {
            return currencyMap.get(rawCurrency);
        }
        return rawCurrency;
    }


    private String getPage(PageType type, WikiUrl page) throws IOException {
        Path pagePath = config.paths.getWikiCachePath().resolve(type.getPath()).resolve(page.getFilename());
        if (!Files.exists(pagePath)) {
            String url = config.vendorCrawler.rootUrl + page.getUrl();
            if (!REST.download(client, url, pagePath)) {
                return "";
            }
        }
        try (Reader reader = Files.newBufferedReader(pagePath)) {
            return CharStreams.toString(reader);
        }
    }

}
