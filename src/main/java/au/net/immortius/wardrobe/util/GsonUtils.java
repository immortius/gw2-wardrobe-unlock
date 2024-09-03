package au.net.immortius.wardrobe.util;

import au.net.immortius.wardrobe.gw2api.entities.ItemData;
import com.google.gson.*;
import io.gsonfire.GsonFireBuilder;

import java.lang.reflect.Type;

public final class GsonUtils {
    private GsonUtils() {

    }

    public static Gson createGson() {
        return new GsonFireBuilder().createGsonBuilder().registerTypeAdapter(ItemData.UnlockItemData.class, new JsonDeserializer<ItemData.UnlockItemData>() {
            @Override
            public ItemData.UnlockItemData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
                ItemData.UnlockItemData result = new ItemData.UnlockItemData();
                if (jsonElement.isJsonArray()) {
                    for (JsonElement element : jsonElement.getAsJsonArray()) {
                        result.unlockItems.add(element.getAsString());
                    }
                } else if (jsonElement.isJsonPrimitive()) {
                    result.unlockItems.add(jsonElement.getAsString());
                } else {
                    throw new JsonParseException("Expected array or string, got " + jsonElement);
                }
                return result;
            }
        }).create();
    }
}
