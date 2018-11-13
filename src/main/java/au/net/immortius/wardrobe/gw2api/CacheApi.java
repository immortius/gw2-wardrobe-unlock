package au.net.immortius.wardrobe.gw2api;

import au.net.immortius.wardrobe.config.CacheConfig;
import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.util.GsonJsonProvider;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.IOException;

/**
 * Caches any new/missing elements from the GW2 api
 */
public class CacheApi {

    private static Logger logger = LoggerFactory.getLogger(CacheApi.class);

    private final Client client;
    private final Gson gson;
    private final Config config;

    public CacheApi() throws IOException {
        this(Config.loadConfig());
    }

    public CacheApi(Config config) {
        this.client = ClientBuilder.newClient();
        this.client.register(GsonJsonProvider.class);
        this.gson = new GsonFireBuilder().createGson();
        this.config = config;
    }

    public CacheApi(Config config, Client client){
        this.client = client;
        this.config = config;
        this.gson = new GsonFireBuilder().createGson();
    }

    public static void main(String... args) throws Exception {
        new CacheApi().run();
    }

    public void run() throws IOException {
        ApiCacher cacher = new ApiCacher(gson, client);

        for (CacheConfig apiCache : config.apiCaches) {
            cacher.cache(apiCache.getBaseUrl(), config.paths.getApiPath().resolve(apiCache.getCachePath()), apiCache.isBulkSupported());
        }
    }
}
