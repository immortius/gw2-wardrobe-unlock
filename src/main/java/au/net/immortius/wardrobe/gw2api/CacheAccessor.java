package au.net.immortius.wardrobe.gw2api;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Accessor class for cached (json) data.
 * @param <T> The type of the cached data
 */
public class CacheAccessor<T> {

    private static final Logger logger = LoggerFactory.getLogger(CacheAccessor.class);

    private Gson gson;
    private Class<T> dataClass;
    private Path cachePath;
    private Predicate<T> filter;

    /**
     * @param gson Gson library to use to deserialize data
     * @param dataClass The class of the data in the cache
     * @param cachePath The cache path
     */
    public CacheAccessor(Gson gson, Class<T> dataClass, Path cachePath) {
        this(gson, dataClass, cachePath, t -> true);
    }

    /**
     * @param gson Gson library to use to deserialize data
     * @param dataClass The class of the data in the cache
     * @param cachePath The cache path
     * @param filter A filter applied to all data returned by this accessor
     */
    public CacheAccessor(Gson gson, Class<T> dataClass, Path cachePath, Predicate<T> filter) {
        this.gson = gson;
        this.dataClass = dataClass;
        this.cachePath = cachePath;
        this.filter = filter;
        try {
            Files.createDirectories(cachePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache path: " + cachePath, e);
        }
    }

    /**
     * @param consumer Consumes all data in the cache
     * @throws IOException
     */
    public void forEach(Consumer<T> consumer) throws IOException {
        try (DirectoryStream<Path> ds = Files.newDirectoryStream(cachePath)) {
            for (Path itemFile : ds) {
                try (Reader itemReader = Files.newBufferedReader(itemFile)) {
                    T itemData = gson.fromJson(itemReader, dataClass);
                    if (filter.test(itemData)) {
                        consumer.accept(itemData);
                    }
                }
            }
        }
    }

    /**
     * @param id
     * @return Retrieves the data for the given id, or {@link Optional#empty)
     */
    public Optional<T> get(String id) {
        Path itemFile = cachePath.resolve(id + ".json");
        if (Files.exists(itemFile)) {
            try (Reader itemReader = Files.newBufferedReader(itemFile)) {
                T data = gson.fromJson(itemReader, dataClass);
                if (filter.test(data)) {
                    return Optional.of(data);
                }
            } catch (IOException e) {
                logger.error("Failed to load {} {}", dataClass.getSimpleName(), id, e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

}
