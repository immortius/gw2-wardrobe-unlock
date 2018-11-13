package au.net.immortius.wardrobe.site.entities;

/**
 * Single component of the cost of an item from a vendor
 */
public class CostComponent {

    public String type;
    public int value;

    public CostComponent(String type, int value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return value + " " + type;
    }
}
