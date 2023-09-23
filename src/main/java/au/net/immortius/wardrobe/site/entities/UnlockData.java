package au.net.immortius.wardrobe.site.entities;

import au.net.immortius.wardrobe.gw2api.Rarity;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Information on a single unlock
 */
public class UnlockData {
    public String id;
    public String name;
    public String image;
    public String color;
    public String type;
    public int xOffset;
    public int yOffset;
    public Rarity rarity;
    public String chatcode;
    public Set<String> sources;
    private List<VendorInfo> vendors;
    public TradingPostEntry priceData;
    public List<UnlockLink> linkedUnlocks = new ArrayList<>();

    /**
     * @return The list of vendors from which this unlock can be purchased
     */
    public List<VendorInfo> getVendors() {
        if (vendors == null) {
            vendors = Lists.newArrayList();
        }
        return vendors;
    }
}
