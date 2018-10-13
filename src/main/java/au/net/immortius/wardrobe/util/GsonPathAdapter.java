package au.net.immortius.wardrobe.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Gson type adapter to handle Paths.
 */
public class GsonPathAdapter extends TypeAdapter<Path> {
    @Override
    public void write(JsonWriter out, Path value) throws IOException {
        out.value(value.toString());
    }

    @Override
    public Path read(JsonReader in) throws IOException {
        return Paths.get(in.nextString());
    }
}
