package au.net.immortius.wardrobe.gw2api.entities;

import com.google.gson.annotations.SerializedName;

import java.util.Set;

/**
 * Common components across multiple multiple return types
 */
public class CommonData {
    public int id;
    public String type;
    public Set<String> flags;
    @SerializedName("chat_link")
    public String chatLink;
}
