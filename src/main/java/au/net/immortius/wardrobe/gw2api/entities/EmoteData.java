package au.net.immortius.wardrobe.gw2api.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class EmoteData {
    public String id;
    @SerializedName("unlock_items")
    public List<String> unlockItems;
}
