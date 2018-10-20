package au.net.immortius.wardrobe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Runs a sequence of processes to update cache data and update the site
 */
public class UpdateSite {
    private static Logger logger = LoggerFactory.getLogger(UpdateSite.class);

    public static void main(String... args) throws Exception {
        new UpdateSite().run();
    }

    public void run() throws IOException {
        new CacheApi().run();
        new CacheIcons().run();
        new MapItemsToUnlocks().run();
        new BuildImageMaps().run();
        new IndexCraftableItems().run();
        new PullCurrentPrices().run();
        new GenerateContent().run();
    }
}
