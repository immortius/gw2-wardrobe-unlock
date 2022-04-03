package au.net.immortius.wardrobe.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CategoryDefinitions {
    private Map<String, Map<String, Set<Integer>>> topLevelCategories;
    private Map<String, Set<Integer>> directGroups;

    public Map<String, Map<String, Set<Integer>>> getTopLevelCategories() {
        if (topLevelCategories == null) {
            topLevelCategories = new LinkedHashMap<>();
        }
        return topLevelCategories;
    }

    public Map<String, Set<Integer>> getDirectGroups() {
        if (directGroups == null) {
            directGroups = new LinkedHashMap<>();
        }
        return directGroups;
    }
}
