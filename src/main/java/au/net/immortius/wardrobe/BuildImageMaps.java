package au.net.immortius.wardrobe;

import au.net.immortius.wardrobe.config.Config;
import au.net.immortius.wardrobe.imagemap.IconAtlas;
import au.net.immortius.wardrobe.imagemap.ImageMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import io.gsonfire.GsonFireBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Builds image maps from the contents of the icon cache
 */
public class BuildImageMaps {
    private static Logger logger = LoggerFactory.getLogger(BuildImageMaps.class);

    private final Gson gson;
    private final Config config;

    public BuildImageMaps() throws IOException {
        this(Config.loadConfig());
    }

    public BuildImageMaps(Config config) {
        this.gson = new GsonFireBuilder().createGson();
        this.config = config;
    }

    public static void main(String... args) throws Exception {
        new BuildImageMaps().run();
    }

    public void run() throws IOException {
        Files.createDirectories(this.config.paths.getAtlasPath());

        List<ImageMap> imageMaps = Lists.newArrayList();
        for (Path imageMapFile : Files.newDirectoryStream(this.config.paths.getAtlasPath())) {
            try (Reader imageMapReader = Files.newBufferedReader(imageMapFile)) {
                imageMaps.add(gson.fromJson(imageMapReader, ImageMap.class));
            }
        }

        IconAtlas atlas = new IconAtlas(config.imageMapper.mapSize, config.imageMapper.iconSize, imageMaps);
        for (Path iconFile : Files.newDirectoryStream(this.config.paths.getIconCachePath())) {
            atlas.addIfMissing(iconFile.getFileName().toString());
        }

        Files.createDirectories(this.config.paths.getImageMapPath());
        Files.createDirectories(this.config.paths.getAtlasPath());
        atlas.save(gson, this.config.paths.getIconCachePath(), this.config.paths.getImageMapPath(), this.config.paths.getAtlasPath());
    }

}
