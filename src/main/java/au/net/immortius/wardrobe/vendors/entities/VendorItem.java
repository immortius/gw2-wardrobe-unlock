package au.net.immortius.wardrobe.vendors.entities;

import au.net.immortius.wardrobe.site.entities.CostComponent;

import java.util.List;

/**
 * Entity for serializing an individual item sold by a vendor
 */
public class VendorItem {
    public String id;
    public List<CostComponent> cost;
}
