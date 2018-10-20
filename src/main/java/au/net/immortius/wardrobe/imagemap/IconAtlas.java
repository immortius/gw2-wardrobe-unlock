package au.net.immortius.wardrobe.imagemap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Manager class for a set of icon maps. This is used to convert all the individual icons used by skins into a small set of large images
 */
public class IconAtlas {

    private final Map<String, ImageMap> imageMapLookup = Maps.newLinkedHashMap();
    private final List<ImageMap> imageMaps;
    private final List<ImageMap> openImageMaps = Lists.newArrayList();

    private final int imageMapSize;
    private final int iconSize;

    /**
     * @param mapSize The size of the image maps in pixels (width & height)
     * @param iconSize The size of icons within the image maps (width & height)
     */
    public IconAtlas(int mapSize, int iconSize) {
        this(mapSize, iconSize, Collections.emptyList());
    }

    /**
     * @param mapSize The size of the image maps in pixels (width & height)
     * @param iconSize The size of icons within the image maps (width & height)
     * @param imageMaps A collection of existing image maps to manage
     */
    public IconAtlas(int mapSize, int iconSize, Collection<ImageMap> imageMaps) {
        this.imageMapSize = mapSize;
        this.iconSize = iconSize;
        this.imageMaps = Lists.newArrayList(imageMaps);
        for (ImageMap imageMap : imageMaps) {
            if (!imageMap.isFull()) {
                openImageMaps.add(imageMap);
            }
            imageMap.getContents().forEach(x -> imageMapLookup.put(x, imageMap));
        }
    }

    /**
     * Add an icon to the image maps, if it is missing. It will be placed in an existing but not full image map
     * if possible, otherwise a new map will be created and the icon added to it
     * @param iconName The name of the icon to add to the maps.
     * @return Whether the icon was added to a map
     */
    public boolean addIfMissing(String iconName) {
        if (imageMapLookup.containsKey(iconName)) {
            return false;
        }
        if (openImageMaps.isEmpty()) {
            ImageMap newImageMap = new ImageMap("image-map-" + imageMaps.size(), imageMapSize, iconSize);
            openImageMaps.add(newImageMap);
            imageMaps.add(newImageMap);
        }
        ImageMap openMap = openImageMaps.get(0);
        openMap.add(iconName);
        imageMapLookup.put(iconName, openMap);
        if (openMap.isFull()) {
            openImageMaps.remove(openMap);
        }
        return true;
    }

    /**
     * Saves the image maps. Only changed image maps will be saved. The images will be created and the icon map contents
     * will be saved
     * @param gson The gson instance to use to save the image map data
     * @param iconSourcePath The location to retrieve cached icons from
     * @param imageMapPath The location to save image maps to
     * @param imageMapDataPath The location to save image map data to
     * @throws IOException
     */
    public void save(Gson gson, Path iconSourcePath, Path imageMapPath, Path imageMapDataPath) throws IOException {
        for (ImageMap map : imageMaps) {
            if (map.isDirty()) {
                map.generateMap(iconSourcePath, imageMapPath);
                try (BufferedWriter mapDataWriter = Files.newBufferedWriter(imageMapDataPath.resolve(map.getName() + ".json"))) {
                    gson.toJson(map, mapDataWriter);
                }

            }
        }
    }


}
