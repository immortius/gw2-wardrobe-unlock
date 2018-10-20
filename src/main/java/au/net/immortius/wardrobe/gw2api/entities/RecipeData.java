package au.net.immortius.wardrobe.gw2api.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Set;

/**
 * Recipe data structure from gw2 endpoint
 */
public class RecipeData extends CommonData {

    @SerializedName("output_item_id")
    public int outputItemId;

    @SerializedName("output_item_count")
    public int outputItemCount;

    @SerializedName("min_rating")
    public int minRating;

    @SerializedName("time_to_craft_ms")
    public int timeToCraftMs;

    public Set<String> disiplines;

    public List<Ingredient> ingredients;
}
