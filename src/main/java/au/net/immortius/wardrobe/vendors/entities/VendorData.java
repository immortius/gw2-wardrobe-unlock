package au.net.immortius.wardrobe.vendors.entities;

import java.util.List;

/**
 * Entity for serializing/deserializing vendor information
 */
public class VendorData {
    public String name;
    public String url;
    public List<VendorItem> items;
}
