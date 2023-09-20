package au.net.immortius.wardrobe.linkedunlocks;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class LinkedUnlocks {
    String name = "";
    Map<String, Set<String>> primary = new LinkedHashMap<>();
    Map<String, Set<String>> secondary = new LinkedHashMap<>();
}
