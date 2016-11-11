import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonWriter;
import entities.Dye;
import entities.Finisher;
import entities.Mini;
import entities.Skin;
import entities.WardrobeContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.GsonJsonProvider;

import javax.imageio.ImageIO;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 */
public class GuaranteedWardrobeUnlockSiteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(GuaranteedWardrobeUnlockSiteBuilder.class);

    private static final String SKINS_URL = "https://api.guildwars2.com/v2/skins";
    private static final String MINIS_URL = "https://api.guildwars2.com/v2/minis";
    private static final String FINISHERS_URL = "https://api.guildwars2.com/v2/finishers";
    private static final String DYES_URL = "https://api.guildwars2.com/v2/colors";
    private static final int THUMB_SIZE = 24;

    private static TypeToken<Set<String>> STRING_SET_TYPE = new TypeToken<Set<String>>() {
    };
    private static TypeToken<Set<Integer>> INTEGER_SET_TYPE = new TypeToken<Set<Integer>>() {};

    private static final Path CACHE_PATH = Paths.get("cache");
    private static final Path INPUT_PATH = Paths.get("input");
    private static final Path SITE_PATH = Paths.get("site");

    private static final Path DYES_PATH = CACHE_PATH.resolve("dyes-data");
    private static final Path DYES_SCREENSHOT_PATH = INPUT_PATH.resolve("dyes-screenshots");

    private static final Path SKINS_SCREENS_PATH = INPUT_PATH.resolve("skins-screenshots");
    private static final Path SKINS_PATH = CACHE_PATH.resolve("skins-data");
    private static final Path SKIN_ICON_PATH = CACHE_PATH.resolve("skin-icons");
    private static final Path SKIN_THUMBNAIL_PATH = CACHE_PATH.resolve("skin-thumbs");

    private static final Path MINIS_SCREENS_PATH = INPUT_PATH.resolve("minis-screenshots");
    private static final Path MINIS_PATH = CACHE_PATH.resolve("minis-data");
    private static final Path MINI_ICON_PATH = CACHE_PATH.resolve("minis-icons");
    private static final Path MINI_THUMBNAIL_PATH = CACHE_PATH.resolve("minis-thumbs");

    private static final Path FINISHERS_SCREENS_PATH = INPUT_PATH.resolve("finishers-screenshots");
    private static final Path FINISHERS_PATH = CACHE_PATH.resolve("finishers-data");
    private static final Path FINISHERS_ICON_PATH = CACHE_PATH.resolve("finishers-icons");
    private static final Path FINISHERS_THUMBNAIL_PATH = CACHE_PATH.resolve("finishers-thumbs");

    private static final Path SKINS_MANIFEST = CACHE_PATH.resolve("skins-manifest.json");
    private static final Path MINIS_MANIFEST = CACHE_PATH.resolve("mini-manifest.json");
    private static final Path FINISHERS_MANIFEST = CACHE_PATH.resolve("finishers-manifest.json");
    private static final Path DYES_MANIFEST = CACHE_PATH.resolve("dyes-manifest.json");
    private static final Path WARDROBE_UNLOCK_CONTENTS = SITE_PATH.resolve("guaranteed-wardrobe-unlock-content.json");

    private static final List<Path> PATHS = Lists.newArrayList(CACHE_PATH, SITE_PATH, SKINS_PATH, SKIN_ICON_PATH, SKIN_THUMBNAIL_PATH, MINIS_PATH, MINI_ICON_PATH, MINI_THUMBNAIL_PATH, FINISHERS_PATH, FINISHERS_ICON_PATH, FINISHERS_THUMBNAIL_PATH, DYES_PATH);


    private Client client = ClientBuilder.newClient();
    private DataCacher dataCacher = new DataCacher(client);
    private IconCacher iconCacher = new IconCacher(client);
    private ThumbnailGenerator thumbnailGenerator = new ThumbnailGenerator();
    private IconMatcher iconMatcher = new IconMatcher();
    private IdResolver idResolver = new IdResolver();
    private Gson gson = new GsonBuilder().create();

    // Skins which unlock together
    // Toxic hands, paldrons
    private Set<Integer> joinedSkins = ImmutableSet.of(1328, 1329, 1330, 1325, 1326, 1327);


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

    // Mini Krait Slaver and Damoss
    // Mini Ice Elemental and Icebrood Goliath
    private Set<Integer> knownSharedMiniIcons = ImmutableSet.of(52, 105, 248, 19);
    private Set<Integer> knownSharedSkinIcons = ImmutableSet.of(
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
        doSkins();
        doMinis();
        doFinishers();
        doDyes();

        generateWardrobeContent();
    }

    private void doSkins() throws Exception {
        obtainSkinInfo();
        obtainSkinIcons();
        generateSkinIconThumbnails();
        matchSkins();
    }

    private void doMinis() throws Exception {
        obtainMiniInfo();
        obtainMiniIcons();
        generateMiniIconThumbnails();
        matchMinis();
    }

    private void doFinishers() throws Exception {
        obtainFinisherInfo();
        obtainFinisherIcons();
        generateFinisherIconThumbnails();
        matchFinishers();
    }

    private void doDyes() throws Exception {
        obtainDyesInfo();
        matchDyes();
    }

    private void matchDyes() {
        if (Files.exists(DYES_MANIFEST)) {
            return;
        }
        Set<Integer> duplicateColors = ImmutableSet.of(1354, 482, 122, 378);
        Set<Integer> ignoreDyes = ImmutableSet.of(1);
        List<Dye> dyes = Lists.newArrayList();
        try (DirectoryStream<Path> dyeFiles = Files.newDirectoryStream(DYES_PATH)) {
            for (Path dyeFile : dyeFiles) {
                try (Reader reader = Files.newBufferedReader(dyeFile)) {
                    Dye dye = gson.fromJson(reader, Dye.class);
                    if (!ignoreDyes.contains(dye.id)) {
                        dyes.add(dye);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load dye files", e);
        }
        Set<Integer> foundDyes = Sets.newLinkedHashSet();
        try (DirectoryStream<Path> screenshotPaths = Files.newDirectoryStream(DYES_SCREENSHOT_PATH)) {
            for (Path screenshotPath : screenshotPaths) {
                BufferedImage screen = ImageIO.read(screenshotPath.toFile());
                int columns = 1 + (screen.getWidth() - 48) / (48 + 10);
                int rows = 1 + (screen.getHeight() - 48) / (48 + 10);
                for (int j = 0; j < rows; ++j) {
                    for (int i = 0; i < columns; ++i) {
                        float r = 0;
                        float g = 0;
                        float b = 0;
                        for (int offX = -3; offX < 4; ++offX) {
                            for (int offY = -3; offY < 4; ++offY) {
                                Color color = new Color(screen.getRGB(24 + i * (48 + 10) + offX, 24 + j * (48 + 10) + offY));
                                r += color.getRed();
                                g += color.getGreen();
                                b += color.getBlue();
                            }
                        }
                        r /= 49;
                        g /= 49;
                        b /= 49;

                        List<Dye> bestMatches = Lists.newArrayList();
                        float bestScore = Integer.MAX_VALUE;
                        for (Dye dye : dyes) {
                            float score = 0;
                            score += Math.abs(dye.cloth.rgb[0] - r);
                            score += Math.abs(dye.cloth.rgb[1] - g);
                            score += Math.abs(dye.cloth.rgb[2] - b);
                            if (score < bestScore && score < 6) {
                                bestScore = score;
                                bestMatches.clear();
                                bestMatches.add(dye);
                            } else if (score == bestScore) {
                                bestMatches.add(dye);
                            }
                        }
                        if (bestMatches.size() == 1) {
                            foundDyes.add(bestMatches.get(0).id);
                        } else if (bestMatches.size() > 1) {

                            for (Dye dye : bestMatches) {
                                if (duplicateColors.contains(dye.id)) {
                                    foundDyes.add(dye.id);
                                } else {
                                    logger.info("Multiple match for ({}, {}) from {} - {}({})", i, j, screenshotPath.getFileName(), dye.name, dye.id);
                                }
                            }
                        } else {
                            logger.info("No matches for ({}, {}) on {}", i, j, screenshotPath.getFileName());
                        }

                    }
                }
            }
        } catch (IOException e) {
            logger.error("Error matching dyes", e);
        }

        try (BufferedWriter writer = Files.newBufferedWriter(DYES_MANIFEST, Charsets.UTF_8)) {
            gson.toJson(foundDyes, INTEGER_SET_TYPE.getType(), writer);
        } catch (IOException e) {
            logger.error("Failed to write dye manifest", e);
        }

    }

    private void generateWardrobeContent() {
        logger.info("Converting Map Icons to Skins...");
        if (!Files.exists(WARDROBE_UNLOCK_CONTENTS)) {
            WardrobeContent wardrobeContent = new WardrobeContent();

            wardrobeContent.skins.addAll(idResolver.collectIds(SKINS_PATH, SKINS_MANIFEST, Skin.class, new Predicate<Skin>() {
                @Override
                public boolean test(Skin skin) {
                    return !(ignoreSkins.contains(skin.id) || Strings.isNullOrEmpty(skin.icon) || Strings.isNullOrEmpty(skin.name)) && !(skin.type.equals("Armor") && skin.details.weight_class.equals("Clothing"));
                }
            }, new Function<List<Skin>, List<Skin>>() {
                @Override
                public List<Skin> apply(List<Skin> skins) {
                    if (skins.size() == 3 && ((skins.get(0).type.equals("Armor") && skins.get(0).name.equals(skins.get(1).name) && skins.get(0).name.equals(skins.get(2).name)) || joinedSkins.contains(skins.get(0).id))) {
                        // Different weight classes, use 0.
                        return Lists.newArrayList(skins.get(0));
                    } else if (skins.size() > 1) {
                        List<Skin> results = Lists.newArrayList();
                        for (Skin skin : skins) {
                            if (!knownSharedSkinIcons.contains(skin.id)) {
                                logger.error("Discovered new shared skin icon on skin '{}' {}", skin.name, skin.id);
                            } else {
                                results.add(skin);
                            }
                        }
                        return results;
                    } else {
                        return Collections.emptyList();
                    }
                }
            }));
            Collections.sort(wardrobeContent.skins);

            wardrobeContent.minis.addAll(idResolver.collectIds(MINIS_PATH, MINIS_MANIFEST, Mini.class, mini -> !Strings.isNullOrEmpty(mini.icon) && !Strings.isNullOrEmpty(mini.name),
                    new Function<List<Mini>, List<Mini>>() {
                        @Override
                        public List<Mini> apply(List<Mini> minis) {
                            if (minis.size() > 1) {
                                List<Mini> results = Lists.newArrayList();
                                for (Mini mini : minis) {
                                    if (!knownSharedMiniIcons.contains(mini.id)) {
                                        logger.error("Discovered new shared mini icon on mini '{}' {}", mini.name, mini.id);
                                    } else {
                                        results.add(mini);
                                    }
                                }
                                return results;
                            } else {
                                return Collections.emptyList();
                            }
                        }
                    }));
            Collections.sort(wardrobeContent.minis);

            wardrobeContent.finishers.addAll(idResolver.collectIds(FINISHERS_PATH, FINISHERS_MANIFEST, Finisher.class, finisher -> !Strings.isNullOrEmpty(finisher.icon) && !Strings.isNullOrEmpty(finisher.name),
                    new Function<List<Finisher>, List<Finisher>>() {
                        @Override
                        public List<Finisher> apply(List<Finisher> finishers) {
                            if (finishers.size() > 1) {
                                List<Finisher> results = Lists.newArrayList();
                                for (Finisher finisher : finishers) {
                                    logger.error("Discovered new shared finisher icon on finisher '{}' {}", finisher.name, finisher.id);
                                }
                                return results;
                            } else {
                                return Collections.emptyList();
                            }
                        }
                    }));
            Collections.sort(wardrobeContent.finishers);


            try (BufferedReader reader = Files.newBufferedReader(DYES_MANIFEST, Charsets.UTF_8)) {
                Set<Integer> set = gson.fromJson(reader, INTEGER_SET_TYPE.getType());
                wardrobeContent.dyes.addAll(set);
                Collections.sort(wardrobeContent.dyes);
            } catch (IOException e) {
                logger.error("Failed to load dye manifest", e);
            }


            try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(WARDROBE_UNLOCK_CONTENTS, Charsets.UTF_8))) {
                gson.toJson(wardrobeContent, WardrobeContent.class, writer);
            } catch (IOException e) {
                logger.error("Failed to save wardrobe contents", e);
            }
        }
    }

    private void matchSkins() {
        logger.info("Comparing skin screenshots to icons...");
        if (Files.exists(SKINS_MANIFEST)) {
            return;
        }

        long start = System.nanoTime();
        Set<String> iconNames = iconMatcher.matchIcons(SKINS_SCREENS_PATH, SKIN_THUMBNAIL_PATH);
        long end = System.nanoTime();
        logger.info("Time taken: {} seconds", ((double) (end - start)) / 1000000000);
        try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(SKINS_MANIFEST, Charsets.UTF_8))) {
            gson.toJson(iconNames, STRING_SET_TYPE.getType(), writer);
        } catch (IOException e) {
            logger.error("Failed to write skins manifest", e);
        }
    }

    private void matchMinis() {
        logger.info("Comparing mini screenshots to icons...");
        if (Files.exists(MINIS_MANIFEST)) {
            return;
        }

        long start = System.nanoTime();
        Set<String> iconNames = iconMatcher.matchIcons(MINIS_SCREENS_PATH, MINI_THUMBNAIL_PATH, 50);
        long end = System.nanoTime();
        logger.info("Time taken: {} seconds", ((double) (end - start)) / 1000000000);
        try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(MINIS_MANIFEST, Charsets.UTF_8))) {
            gson.toJson(iconNames, STRING_SET_TYPE.getType(), writer);
        } catch (IOException e) {
            logger.error("Failed to write minis manifest", e);
        }
    }

    private void matchFinishers() {
        logger.info("Comparing finisher screenshots to icons...");
        if (Files.exists(FINISHERS_MANIFEST)) {
            return;
        }

        long start = System.nanoTime();
        Set<String> iconNames = iconMatcher.matchIcons(FINISHERS_SCREENS_PATH, FINISHERS_THUMBNAIL_PATH);
        long end = System.nanoTime();
        logger.info("Time taken: {} seconds", ((double) (end - start)) / 1000000000);
        try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(FINISHERS_MANIFEST, Charsets.UTF_8))) {
            gson.toJson(iconNames, STRING_SET_TYPE.getType(), writer);
        } catch (IOException e) {
            logger.error("Failed to write finisher manifest", e);
        }
    }

    private void createDirectories() throws IOException {
        System.out.println("Creating output directories...");
        for (Path path : PATHS) {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        }
    }

    private void obtainDyesInfo() throws IOException {
        logger.info("Obtaining dyes data...");
        int downloaded = dataCacher.downloadData(DYES_URL, DYES_PATH, Collections.emptySet());
        logger.info("Downloaded {} dyes.", downloaded);
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

    private void obtainMiniInfo() throws IOException {
        logger.info("Obtaining mini data...");
        int downloaded = dataCacher.downloadData(MINIS_URL, MINIS_PATH, Collections.emptySet());
        logger.info("Downloaded {} minis.", downloaded);
    }

    private void obtainMiniIcons() {
        logger.info("Obtaining mini icons...");
        int downloaded = iconCacher.cacheIconsForData(MINIS_PATH, MINI_ICON_PATH);
        logger.info("Downloaded {} icons.", downloaded);
    }

    private void generateMiniIconThumbnails() {
        logger.info("Generating thumbnails for mini icons");
        thumbnailGenerator.generateThumbs(MINI_ICON_PATH, MINI_THUMBNAIL_PATH, THUMB_SIZE);
    }

    private void obtainFinisherInfo() throws IOException {
        logger.info("Obtaining finisher data...");
        int downloaded = dataCacher.downloadData(FINISHERS_URL, FINISHERS_PATH, Collections.emptySet());
        logger.info("Downloaded {} finishers.", downloaded);
    }

    private void obtainFinisherIcons() {
        logger.info("Obtaining finisher icons...");
        int downloaded = iconCacher.cacheIconsForData(FINISHERS_PATH, FINISHERS_ICON_PATH);
        logger.info("Downloaded {} icons.", downloaded);
    }

    private void generateFinisherIconThumbnails() {
        logger.info("Generating thumbnails for finisher icons");
        thumbnailGenerator.generateThumbs(FINISHERS_ICON_PATH, FINISHERS_THUMBNAIL_PATH, THUMB_SIZE);
    }

}
