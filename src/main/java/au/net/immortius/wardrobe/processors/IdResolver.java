package au.net.immortius.wardrobe.processors;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import au.net.immortius.wardrobe.entities.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
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

    private Gson gson;

    public IdResolver(Gson gson) {
        this.gson = gson;
    }

    public Set<Integer> collectIds(Path dataPath, Path iconsList, Class<? extends Data> type, Set<Integer> ignoreIds, Set<Integer> iconSharingIds, Set<Integer> joinedIds, Predicate<Data> dataFilter, Function<List<Data>, List<Data>> resultFilter) {
        SetMultimap<String, Data> dataMap = mapIconsToData(dataPath, type, ignoreIds, dataFilter);

        Set<Integer> collectedIds = Sets.newLinkedHashSet();
        try (JsonReader reader = new JsonReader(Files.newBufferedReader(iconsList, Charsets.UTF_8))) {
            Set<String> iconNames = gson.fromJson(reader, STRING_SET_TYPE.getType());
            for (String iconName : iconNames) {
                List<Data> data = dataMap.get(iconName).stream().collect(Collectors.toList());

                if (data.isEmpty()) {
                    logger.error("Matched no skins");
                } else if (data.size() == 1) {
                    collectedIds.add(data.get(0).id);
                } else {
                    data = resultFilter.apply(data);
                    if (data.size() == 1) {
                        collectedIds.add(data.get(0).id);
                    } else if (joinedIds.contains(data.get(0).id)) {
                        collectedIds.add(data.get(0).id);
                    } else {
                        for (Data item : data) {
                            if (!iconSharingIds.contains(item.id)) {
                                logger.error("Discovered new shared skin icon on skin '{}' {}", item.name, item.id);
                            } else {
                                collectedIds.add(item.id);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to generate wardrobe contents", e);
        }
        return collectedIds;
    }

    private SetMultimap<String, Data> mapIconsToData(Path dataPath, Class<? extends Data> type, Set<Integer> ignoreIds, Predicate<Data> filter) {
        SetMultimap<String, Data> iconToDataMap = HashMultimap.create();
        try (DirectoryStream<Path> files = Files.newDirectoryStream(dataPath)) {
            for (Path dataFile : files) {
                try (Reader reader = Files.newBufferedReader(dataFile)) {
                    Data data = gson.fromJson(reader, type);
                    if (!Strings.isNullOrEmpty(data.icon) && !Strings.isNullOrEmpty(data.name) && !ignoreIds.contains(data.id) && filter.test(data)) {
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
