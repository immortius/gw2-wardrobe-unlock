package au.net.immortius.wardrobe.config;

import java.nio.file.Path;

/**
 * Paths for all the different files. Many paths are resolved relative to either the input path, cache path or api path
 */
public class PathsConfig {

    public Path baseInputPath;
    public Path baseCachePath;
    private Path apiRelativePath;
    private Path relativeRecipesPath;
    private Path craftableItemsFile;
    private Path relativeSkinsPath;
    private Path relativeItemPath;
    private Path relativeUnlockItemsPath;
    public Path contentFile;
    private Path relativeVendorsPath;
    private Path relativeGwuPath;
    private Path relativePricesPath;
    private Path relativeIconCachePath;
    private Path relativeAtlasPath;
    private Path relativeImageMapPath;
    private Path relativeThumbnailsPath;
    public Path siteImagePath;
    public Path siteDataPath;
    private Path relativeWikiCachePath;

    public Path getApiPath() {
        return baseCachePath.resolve(apiRelativePath);
    }

    public Path getRecipesPath() {
        return getApiPath().resolve(relativeRecipesPath);
    }

    public Path getCraftableItemsFile() {
        return baseCachePath.resolve(craftableItemsFile);
    }

    public Path getSkinsPath() {
        return getApiPath().resolve(relativeSkinsPath);
    }

    public Path getItemPath() {
        return getApiPath().resolve(relativeItemPath);
    }

    public Path getUnlockItemsPath() {
        return baseCachePath.resolve(relativeUnlockItemsPath);
    }

    public Path getVendorsPath() {
        return baseCachePath.resolve(relativeVendorsPath);
    }

    public Path getGuaranteedWardrobeUnlocksPath() {
        return baseCachePath.resolve(relativeGwuPath);
    }

    public Path getPricesPath() {
        return getApiPath().resolve(relativePricesPath);
    }

    public Path getUnlockPricesPath() {
        return baseCachePath.resolve(relativePricesPath);
    }

    public Path getIconCachePath() { return getApiPath().resolve(relativeIconCachePath); }

    public Path getAtlasPath() { return baseCachePath.resolve(relativeAtlasPath); }

    public Path getImageMapPath() { return baseCachePath.resolve(relativeImageMapPath); }

    public Path getThumbnailPath() {
        return baseCachePath.resolve(relativeThumbnailsPath);
    }

    public Path getWikiCachePath() { return baseCachePath.resolve(relativeWikiCachePath); }

}
