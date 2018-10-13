package au.net.immortius.wardrobe.config;

import java.nio.file.Path;

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
}
