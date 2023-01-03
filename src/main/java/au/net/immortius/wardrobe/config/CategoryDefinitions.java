package au.net.immortius.wardrobe.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class CategoryDefinitions {
    private Map<String, Map<String, Set<String>>> topLevelCategories;
    private Map<String, Set<String>> directGroups;

    public Map<String, Map<String, Set<String>>> getTopLevelCategories() {
        if (topLevelCategories == null) {
            topLevelCategories = new LinkedHashMap<>();
        }
        return topLevelCategories;
    }

    public Map<String, Set<String>> getDirectGroups() {
        if (directGroups == null) {
            directGroups = new LinkedHashMap<>();
        }
        return directGroups;
    }
}
