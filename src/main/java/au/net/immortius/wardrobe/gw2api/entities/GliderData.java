package au.net.immortius.wardrobe.gw2api.entities;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;

import java.util.Arrays;
import java.util.List;

public class GliderData extends CommonData {

    @SerializedName("unlock_items")
    private String[] unlockItems;

    public List<String> getUnlockItems() {
        List<String> unlockItems = Lists.newArrayList();
        if (this.unlockItems != null) {
            unlockItems.addAll(Arrays.asList(this.unlockItems));
        }
        return unlockItems;
    }
}
