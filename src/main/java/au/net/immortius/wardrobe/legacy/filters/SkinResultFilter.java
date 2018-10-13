package au.net.immortius.wardrobe.legacy.filters;

import au.net.immortius.wardrobe.legacy.entities.Skin;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Function;

/**
 *
 */
public class SkinResultFilter implements Function<List<Skin>, List<Skin>> {
    @Override
    public List<Skin> apply(List<Skin> skins) {
        if (skins.size() == 3 && ((skins.get(0).type.equals("Armor") && skins.get(0).name.equals(skins.get(1).name) && skins.get(0).name.equals(skins.get(2).name)))) {
            // Different weight classes, use 0.
            return Lists.newArrayList(skins.get(0));
        }
        return skins;
    }
}
