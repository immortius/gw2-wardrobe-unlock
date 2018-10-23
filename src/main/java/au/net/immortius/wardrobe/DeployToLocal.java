package au.net.immortius.wardrobe;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.util.NioUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DeployToLocal {
    private static Logger logger = LoggerFactory.getLogger(DeployToLocal.class);

    private final Config config;

    public DeployToLocal() throws IOException {
        this(Config.loadConfig());
    }

    public DeployToLocal(Config config) {
        this.config = config;
    }

    public static void main(String ... args) throws Exception {
        new DeployToLocal().run();
    }

    public void run() throws IOException {
        logger.info("Deploying image maps and content to local site");
        NioUtils.copyPathContents(config.paths.getImageMapPath(), config.paths.siteImagePath);
        Files.copy(config.paths.contentFile, config.paths.siteDataPath.resolve(config.paths.contentFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
    }


}
