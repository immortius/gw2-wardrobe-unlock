package au.net.immortius.wardrobe.entities;

import java.util.Collections;
import java.util.Set;

/**
 *
 */
public class DyeConfig {
    public String id;
    public String url;
    private Set<Integer> ignoreIds;
    private Set<Integer> duplicateColors;

    public Set<Integer> getIgnoreIds() {
        if (ignoreIds == null) {
            return Collections.emptySet();
        }
        return ignoreIds;
    }

    public Set<Integer> getDuplicateColors() {
        if (duplicateColors == null) {
            return Collections.emptySet();
        }
        return duplicateColors;
    }
}
