package au.net.immortius.wardrobe.site.entities;

import javax.annotation.Nullable;

/**
 * Trading post price information for an unlock
 */
public class PriceEntry {
    private final String itemId; //Actual item, since some items share skins
    private final Integer price;
    @Nullable
    private String itemName;

    public PriceEntry(String itemId, Integer price) {
        this.itemId = itemId;
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public Integer getPrice() {
        return price;
    }

    @Nullable
    public String getItemName() {
        return itemName;
    }

    public void setItemName(@Nullable String itemName) {
        this.itemName = itemName;
    }
}
