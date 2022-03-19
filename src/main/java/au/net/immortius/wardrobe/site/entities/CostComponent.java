package au.net.immortius.wardrobe.site.entities;

import java.util.Objects;

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CostComponent that = (CostComponent) o;
        return value == that.value && Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value);
    }

    @Override
    public String toString() {
        return value + " " + type;
    }
}
