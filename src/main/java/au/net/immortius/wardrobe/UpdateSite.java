package au.net.immortius.wardrobe;

import au.net.immortius.wardrobe.gw2api.CacheApi;
import au.net.immortius.wardrobe.gw2api.CacheIcons;
import au.net.immortius.wardrobe.gw2api.PullCurrentPrices;
import au.net.immortius.wardrobe.gwu.AnalyseGWUItems;
import au.net.immortius.wardrobe.imagemap.BuildImageMaps;
import au.net.immortius.wardrobe.site.GenerateContent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a sequence of processes to update cache data and update the site
 */
public class UpdateSite {
    private static Logger logger = LoggerFactory.getLogger(UpdateSite.class);

    public static void main(String... args) throws Exception {
        Options options = new Options();
        options.addOption("username", true, "The username for uploading to an ftp site");
        options.addOption("password", true, "The password for uploading to an ftp site");
        options.addOption("address", true, "The address for uploading to an ftp site");
        options.addOption("analyseGwu", false, "Whether to rebuild the guaranteed wardrobe unlock data");
        options.addOption("useCachedPrices", false, "Whether to use cached prices rather than obtaining fresh");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        new CacheApi().run();
        new CacheIcons().run();
        new MapItemsToUnlocks().run();
        new BuildImageMaps().run();
        new IndexCraftableItems().run();
        if (!cmd.hasOption("useCachedPrices")) {
            new PullCurrentPrices().run();
        }
        if (cmd.hasOption("analyseGwu")) {
            new AnalyseGWUItems().run();
        }
        new GenerateContent().run();
        new DeployToLocal().run();
        if (cmd.hasOption("username") && cmd.hasOption("password") && cmd.hasOption("address")) {
            new DeployToRemote(cmd.getOptionValue("address"), cmd.getOptionValue("username"), cmd.getOptionValue("password")).run();
        }
    }

}
