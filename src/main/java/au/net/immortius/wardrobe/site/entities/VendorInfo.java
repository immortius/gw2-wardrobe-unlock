package au.net.immortius.wardrobe.site.entities;

import java.util.List;
import java.util.Objects;

/**
 * Information on a vendor that sells an unlock
 */
public class VendorInfo {
    public String vendorName;
    public String vendorUrl;
    public List<CostComponent> cost;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VendorInfo that = (VendorInfo) o;
        return Objects.equals(vendorName, that.vendorName) && Objects.equals(cost, that.cost);
    }

    @Override
    public int hashCode() {
        return Objects.hash(vendorName, cost);
    }
}
