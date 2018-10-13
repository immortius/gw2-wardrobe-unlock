package au.net.immortius.wardrobe.config;

import com.google.common.collect.Lists;

import java.util.List;

public class ItemUnlockMapperConfig {
    private List<ItemDetailUnlockMapping> unlockDetailsMappings;

    public List<ItemDetailUnlockMapping> getUnlockDetailsMappings() {
        if (unlockDetailsMappings == null) {
            unlockDetailsMappings = Lists.newArrayList();
        }
        return unlockDetailsMappings;
    }
}
