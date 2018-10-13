package au.net.immortius.wardrobe.gw2api.entities;

import com.google.gson.annotations.SerializedName;

/**
 * Ingrediant structure (as part of a recipe
 */
public class Ingredient {
    @SerializedName("itemId")
    public int itemId;

    public int count;
}
