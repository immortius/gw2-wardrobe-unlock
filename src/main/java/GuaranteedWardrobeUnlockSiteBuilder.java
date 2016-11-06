import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import entities.Skin;
import entities.WardrobeContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.GsonJsonProvider;

import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 */
public class GuaranteedWardrobeUnlockSiteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(GuaranteedWardrobeUnlockSiteBuilder.class);

    private static final String SKINS_URL = "https://api.guildwars2.com/v2/skins";
    private static final int THUMB_SIZE = 24;
    private static final int PREVIEW_ICON_SIZE = 24;
    private static final int H_BORDER = 5;

    private static GenericType<int[]> INT_ARRAY_TYPE = new GenericType<int[]>() {
    };
    private static TypeToken<Set<String>> STRING_SET_TYPE = new TypeToken<Set<String>>() {
    };
    private static TypeToken<Set<Integer>> INTEGER_SET_TYPE = new TypeToken<Set<Integer>>() {
    };
    private static TypeToken<List<String>> STRING_LIST_TYPE = new TypeToken<List<String>>() {
    };


    private static final Path CACHE_PATH = Paths.get("cache");
    private static final Path INPUT_PATH = Paths.get("input");
    private static final Path SITE_PATH = Paths.get("site");
    private static final Path EQUIPMENT_SCREENS_PATH = INPUT_PATH.resolve("equipment");
    private static final Path SKINS_PATH = CACHE_PATH.resolve("skins");
    private static final Path SKIN_ICON_PATH = CACHE_PATH.resolve("skin-icons");
    private static final Path SKIN_THUMBNAIL_PATH = CACHE_PATH.resolve("skin-thumbs");
    private static final Path EQUIPMENT_MANIFEST = CACHE_PATH.resolve("found-equipment.json");
    private static final Path EQUIPMENT_SKIN_MANIFEST = CACHE_PATH.resolve("found-equipment-skins.json");
    private static final Path WARDROBE_UNLOCK_CONTENTS = SITE_PATH.resolve("guaranteed-wardrobe-unlock-content.json");


    private Client client = ClientBuilder.newClient();
    private DataCacher dataCacher = new DataCacher(client);
    private IconCacher iconCacher = new IconCacher(client);
    private ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();
    private IconMatcher iconMatcher = new IconMatcher();
    private Gson gson = new GsonBuilder().create();

    // Skins which unlock together
    // Toxic hands, paldrons
    private Set<Integer> sharedSkins = ImmutableSet.of(1328, 1329, 1330, 1325, 1326, 1327);

    private Set<Integer> ignoreSkins = ImmutableSet.of(
            // Explicitly ignore Guild Cavalier weapons (share icons with Cavalier weapons, but aren't included)
            6766, 6761, 6750, 6755, 6781, 6748, 6770, 6763, 6762, 6771, 6773, 6778, 6782, 6769, 6754, 6776
            // Explicitly ignore Darksteel and Mithril (share icons with Bronze/Iron/Steel, but aren't included) and Elder/Hard
            , 4090, 4103, 4073, 4043, 4105, 4094, 3917, 3957, 3950, 3919, 4036, 4006, 3956, 3988, 4102, 4087, 4055, 4081, 4046, 4008, 3968, 3998, 4061, 4079, 3992, 4034, 3969, 3990, 4019, 3972, 4024, 3987, 3965, 3934, 4114, 4107, 4049, 4083
            // The Maelstrom shares an icon with Aureate Staff
            , 4747
            // Ironfist? mace shares with bronze/iron/steel
            , 4879
            // Adamant Guard Trident shares icon with Staff, but not included
            , 4282
            // Panscopic Monocle same as Leather Mask
            , 2053
            // Wolfborn Rifle same icon as Wolfborn Speargun, but terrestial weapons not included
            , 4305
            // Black Earth Trident same as Krait Crook
            , 4712
            // Air-Filtration devices share an icon with Aquabreathers
            , 2021, 2022, 2023
            // Guild Rifle isn't, but Guild Speargun is???
            , 4544
            // Not all the weight classes of Black Earth Aquabreather
            , 859, 857
            // Steam speargun isn't really a thing
            , 5091
            // No OooOoo shoorter
            , 5362
            // No Godskull harpoon gun
            , 4986
            // No Guild Defender Shoulderplate
            , 1561
            // Devout not Guild Archamge
            , 1671, 1824, 1714, 1830, 1675
    );

    private Set<Integer> knownSharedIcons = ImmutableSet.of(
            // Seraph Staff, Seraph Trident,
            4349, 4291
            // Iron Sword, Bronze Sword, Steel Sword,
            , 4062, 4047, 5691
// Modniir Boomstick, Modniir Harpoon Gun,
            , 4608, 4631
// Iron Spear, Bronze Spear, Steel Spear,
            , 4000, 3977, 5695
// Seasoned Wood Staff, Green Wood Staff, Soft Wood Staff,
            , 5688, 4054, 4067
// Seasoned Wood Torch, Soft Wood Torch, Green Wood Torch,
            , 5681, 3890, 3883
// Steam Trident, Steam Staff,
            , 5144, 5238
// Peacemaker's Speargun, Peacemaker's Rifle,
            , 4288, 4273
// Wolfborn Staff, Wolfborn Trident,
            , 4344, 4286
// Warden Speargun, Warden Rifle,
            , 4292, 4336
// Black Earth Aquabreather, Stately Circlet,
            , 858, 894
// Dredge Boomstick, Dredge Harpoon Gun,
            , 4104, 3953
// Green Wood Focus, Soft Wood Focus, Seasoned Wood Focus,
            , 3880, 3887, 5683
// Iron Mace, Steel Mace, Bronze Mace,
            , 3964, 5696, 3945
// Leather Aquabreather, Metal Aquabreather, Cloth Aquabreather,
            , 856, 854, 855
// Dredge Trident, Dredge Pillar,
            , 3928, 4088
// Pirate Needler, Pirate Crescent,
            , 4649, 4659
// Green Wood Trident, Soft Wood Trident, Seasoned Wood Trident,
            , 3889, 3899, 5679
// Deathly Bull's Pauldrons, Deathly Bull's Mantle, Deathly Bull's Shoulderpads,
            , 1119, 1123, 1131
// Bronze Hammer, Iron Hammer, Steel Hammer,
            , 4048, 4060, 5694
// Bronze Greatsword, Steel Greatsword, Iron Greatsword,
            , 4005, 5693, 4015
// Deathly Pauldrons, Deathly Shoulderpads, Deathly Mantle,
            , 1120, 1136, 1126
// Bronze Axe, Iron Axe, Steel Axe,
            , 3946, 3962, 5682
// Adamant Guard Cannon, Adamant Guard Rifle,
            , 4281, 4301
// Lionguard Cannon, Lionguard Rifle,
            , 4636, 4625
// Seasoned Wood Warhorn, Green Wood Warhorn, Soft Wood Warhorn,
            , 5680, 3898, 3907
// Steel Dagger, Iron Dagger, Bronze Dagger,
            , 5692, 4009, 3994
// Ogre Trident, Ogre Warstaff,
            , 4001, 4101
// Bronze Pistol, Steel Pistol, Iron Pistol,
            , 3932, 5690, 3875
// Deathly Avian Pauldrons, Deathly Avian Shoulderpads, Deathly Avian Mantle,
            , 1116, 1128, 1121
// Ebon Vanguard Spear, Ebon Vanguard Dagger,
            , 4653, 4615
// Aetherized Longbow, Aetherized Short Bow,
            , 3749, 3715
// Green Wood Short Bow, Soft Wood Short Bow, Seasoned Wood Short Bow,
            , 3900, 3906, 5687
// Green Wood Harpoon Gun, Seasoned Wood Harpoon Gun, Soft Wood Harpoon Gun,
            , 3908, 5684, 3927
// Modniir Quarterstaff, Modniir Impaler, Modniir Trident,
            , 4613, 4645, 4634
// Ebon Vanguard Trident, Ebon Vanguard Staff,
            , 4640, 4638
// Heavy Island Shoulder, Light Island Shoulder, Medium Island Shoulder,
            , 1137, 1141, 1143
// Shadow Shield, Shadow Axe,
            , 5902, 5907
// Green Wood Longbow, Seasoned Wood Longbow, Soft Wood Longbow,
            , 3921, 5685, 3942
// Lionguard Staff, Lionguard Trident,
            , 4628, 4639
// Ogre Harpoon Gun, Ogre Blaster,
            , 4025, 4110
// Seraph Speargun, Seraph Rifle,
            , 4285, 4333
// Soft Wood Scepter, Seasoned Wood Scepter, Green Wood Scepter,
            , 3892, 5686, 3895
// Guild Short Bow, Guild Longbow,
            , 4542, 4546
// Bronze Rifle, Iron Rifle, Steel Rifle,
            , 4076, 4092, 5697
// Bronze Shield, Iron Shield, Steel Shield,
            , 4004, 4012, 5689
    );


    public static void main(String[] args) throws Exception {
        new GuaranteedWardrobeUnlockSiteBuilder().run();
    }

    public void run() throws Exception {
        client.register(GsonJsonProvider.class);
        createDirectories();
        obtainSkinInfo();
        obtainSkinIcons();
        generateSkinIconThumbnails();

        matchSkins();
        generateWardrobeContent();
    }

    private void generateWardrobeContent() {
        SetMultimap<String, Integer> iconToSkinMap = buildIconToSkinMaps();

        System.out.println("Converting Map Icons to Skins...");
        if (!Files.exists(WARDROBE_UNLOCK_CONTENTS)) {
            Set<Integer> collectedSkinIds = Sets.newLinkedHashSet();
            List<String> collectedSkinNames = Lists.newArrayList();
            try (JsonReader reader = new JsonReader(Files.newBufferedReader(EQUIPMENT_MANIFEST, Charsets.UTF_8))) {
                Set<String> iconNames = gson.fromJson(reader, STRING_SET_TYPE.getType());
                for (String iconName : iconNames) {
                    Set<Integer> skinIds = iconToSkinMap.get(iconName);
                    List<Skin> skins = skinIds.stream().map(this::loadSkin).collect(Collectors.toList());
                    if (skins.size() == 3 && ((skins.get(0).type.equals("Armor") && skins.get(0).name.equals(skins.get(1).name) && skins.get(0).name.equals(skins.get(2).name)) || sharedSkins.contains(skins.get(0).id))) {
                        // Different weight classes, use 0.
                        collectedSkinIds.add(skins.get(0).id);
                        collectedSkinNames.add(skins.get(0).name);
                    } else if (skinIds.size() > 1) {
                        for (Skin skin : skins) {
                            if (!knownSharedIcons.contains(skin.id)) {
                                System.out.println("Discovered new shared skin icon on skin '" + skin.name + "' " + skin.id);
                            } else {
                                collectedSkinIds.add(skin.id);
                                collectedSkinNames.add(skin.name);
                            }
                        }
                    } else if (skinIds.size() == 1) {
                        collectedSkinIds.add(skins.get(0).id);
                        collectedSkinNames.add(skins.get(0).name);
                    } else if (skinIds.isEmpty()) {
                        System.out.println("Matched no skins");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            WardrobeContent content = new WardrobeContent();
            content.equipmentSkins.addAll(collectedSkinIds);
            Collections.sort(content.equipmentSkins);

            try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(EQUIPMENT_SKIN_MANIFEST, Charsets.UTF_8))) {
                gson.toJson(collectedSkinNames, STRING_LIST_TYPE.getType(), writer);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(WARDROBE_UNLOCK_CONTENTS, Charsets.UTF_8))) {
                gson.toJson(content, WardrobeContent.class, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void matchSkins() {
        logger.info("Comparing screenshots to icons...");
        if (Files.exists(EQUIPMENT_MANIFEST)) {
            return;
        }

        long start = System.nanoTime();
        Set<String> iconNames = iconMatcher.matchIcons(EQUIPMENT_SCREENS_PATH, SKIN_THUMBNAIL_PATH);
        long end = System.nanoTime();
        logger.info("Time taken: {} seconds", ((double) (end - start)) / 1000000000);
        try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(EQUIPMENT_MANIFEST, Charsets.UTF_8))) {
            gson.toJson(iconNames, STRING_SET_TYPE.getType(), writer);
        } catch (IOException e) {
            logger.error("Failed to write equipment manifest", e);
        }
    }

    private SetMultimap<String, Integer> buildIconToSkinMaps() {
        System.out.println("Building icon->skin mappings...");
        SetMultimap<String, Integer> iconToSkinMap = HashMultimap.create();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(SKINS_PATH)) {
            for (Path skinFile : files) {
                try (JsonReader reader = new JsonReader(Files.newBufferedReader(skinFile))) {
                    Skin skin = gson.fromJson(reader, Skin.class);
                    if (!ignoreSkins.contains(skin.id) && !Strings.isNullOrEmpty(skin.icon) && !Strings.isNullOrEmpty(skin.name)) {
                        if (skin.type.equals("Armor") && skin.details.weight_class.equals("Clothing")) {
                            continue;
                        }
                        iconToSkinMap.put(skin.getIconName(), skin.id);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return iconToSkinMap;
    }

    private Skin loadSkin(int id) {
        try (JsonReader reader = new JsonReader(Files.newBufferedReader(SKINS_PATH.resolve(id + ".json")))) {
            return gson.fromJson(reader, Skin.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load skin", e);
        }
    }


    private void createDirectories() throws IOException {
        System.out.println("Creating output directories...");
        if (!Files.exists(SKINS_PATH)) {
            Files.createDirectory(SKINS_PATH);
        }
        if (!Files.exists(SKIN_ICON_PATH)) {
            Files.createDirectory(SKIN_ICON_PATH);
        }
        if (!Files.exists(SKIN_THUMBNAIL_PATH)) {
            Files.createDirectory(SKIN_THUMBNAIL_PATH);
        }
        if (!Files.exists(SITE_PATH)) {
            Files.createDirectory(SITE_PATH);
        }
    }

    private void obtainSkinInfo() throws IOException {
        logger.info("Obtaining skin data...");
        int downloaded = dataCacher.downloadData(SKINS_URL, SKINS_PATH, ignoreSkins);
        logger.info("Downloaded {} skins.", downloaded);
    }

    private void obtainSkinIcons() {
        logger.info("Obtaining skin icons...");
        int downloaded = iconCacher.cacheIconsForData(SKINS_PATH, SKIN_ICON_PATH);
        logger.info("Downloaded {} icons.", downloaded);
    }

    private void generateSkinIconThumbnails() {
        logger.info("Generating thumbnails for skin icons");
        thumbnailGenerator.generateThumbs(SKIN_ICON_PATH, SKIN_THUMBNAIL_PATH, THUMB_SIZE);
    }

}
