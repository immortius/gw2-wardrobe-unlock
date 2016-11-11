import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import entities.Common;
import entities.Skin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 */
public class IdResolver {

    private static final Logger logger = LoggerFactory.getLogger(IdResolver.class);

    private static TypeToken<Set<String>> STRING_SET_TYPE = new TypeToken<Set<String>>() {
    };

    private Gson gson = new GsonBuilder().create();

    public <T extends Common> Set<Integer> collectIds(Path dataPath, Path iconsList, Class<T> type, Predicate<T> dataFilter, Function<List<T>, List<T>> multiResolver) {
        SetMultimap<String, T> dataMap = mapIconsToData(dataPath, type, dataFilter);

        Set<Integer> collectedIds = Sets.newLinkedHashSet();
        try (JsonReader reader = new JsonReader(Files.newBufferedReader(iconsList, Charsets.UTF_8))) {
            Set<String> iconNames = gson.fromJson(reader, STRING_SET_TYPE.getType());
            for (String iconName : iconNames) {
                List<T> data = dataMap.get(iconName).stream().collect(Collectors.toList());

                if (data.isEmpty()) {
                    logger.error("Matched no skins");
                } else if (data.size() == 1) {
                    collectedIds.add(data.get(0).id);
                } else {
                    List<T> result = multiResolver.apply(data);
                    if (!result.isEmpty()) {
                        collectedIds.addAll(result.stream().map(t -> t.id).collect(Collectors.toList()));
                    } else {
                        logger.error("Unable to resolve multiple ids for {} - {}", iconName, data.stream().map(t -> t.id + " - " + t.name).collect(Collectors.toList()));
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to generate wardrobe contents", e);
        }
        return collectedIds;
    }

    private <T extends Common> SetMultimap<String, T> mapIconsToData(Path dataPath, Class<T> type, Predicate<T> filter) {
        SetMultimap<String, T> iconToDataMap = HashMultimap.create();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataPath)) {
            for (Path dataFile : files) {
                try (JsonReader reader = new JsonReader(Files.newBufferedReader(dataFile))) {
                    T data = gson.fromJson(reader, type);
                    if (!Strings.isNullOrEmpty(data.icon) && !Strings.isNullOrEmpty(data.name) && filter.test(data)) {
                        iconToDataMap.put(data.getIconName(), data);
                    }
                } catch (IOException e) {
                    logger.error("Failed to read data", e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read data", e);
        }
        return iconToDataMap;
    }
}
