package au.net.immortius.wardrobe.vendors;

import au.net.immortius.wardrobe.site.entities.CostComponent;

import java.util.List;

/**
 * Entity for serializing an individual item sold by a vendor
 */
public class VendorItem {
    public int id;
    public String category;
    public List<CostComponent> cost;
}
