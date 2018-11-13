package au.net.immortius.wardrobe.gw2api.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Item details structure - combines detail structures from multiple endpoints for generic processing
 */
public class ItemDetailsData {
    public String type;
    public int[] skins;
    @SerializedName("guild_upgrade_id")
    public int guildUpgradeId;
    @SerializedName("unlock_type")
    public String unlockType;
    @SerializedName("color_id")
    public int colorId;
    @SerializedName("minipet_id")
    public Integer minipetId;
}
