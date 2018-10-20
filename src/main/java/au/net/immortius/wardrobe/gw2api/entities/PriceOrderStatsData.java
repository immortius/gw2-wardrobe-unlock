package au.net.immortius.wardrobe.gw2api.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Price information for either buy or sell orders
 */
public class PriceOrderStatsData {
    public int quantity;
    @SerializedName("unit_price")
    public int unitPrice;
}
