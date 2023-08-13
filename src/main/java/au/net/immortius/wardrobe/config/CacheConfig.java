package au.net.immortius.wardrobe.config;

import java.nio.file.Path;

/**
 * Configuration for caching from a GW2 api
 */
public class CacheConfig {
    private String baseUrl;
    private Path cachePath;
    private boolean bulkSupported;
    private boolean stringIds;
    private boolean groupedSkins;

    /**
     * @return The gw2 api url for the endpoint
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @return The path to which the api should be cached
     */
    public Path getCachePath() {
        return cachePath;
    }

    /**
     * @return Does this endpoint support the ids=all functionality
     */
    public boolean isBulkSupported() {
        return bulkSupported;
    }

    public boolean isStringIds() {
        return stringIds;
    }

    public boolean isGroupedSkins() {
        return groupedSkins;
    }
}
