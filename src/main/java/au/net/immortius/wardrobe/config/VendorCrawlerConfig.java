package au.net.immortius.wardrobe.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class VendorCrawlerConfig {
    public String rootUrl;

    public Set<String> skinTypes;

    private List<String> categoryPages;

    private List<String> vendorPages;

    private Set<String> ignorePages;

    public List<String> getCategoryPages() {
        if (categoryPages == null) {
            categoryPages = Lists.newArrayList();
        }
        return categoryPages;
    }

    public List<String> getVendorPages() {
        if (vendorPages == null) {
            vendorPages = Lists.newArrayList();
        }
        return vendorPages;
    }

    public Set<String> getIgnorePages() {
        if (ignorePages == null) {
            ignorePages = Sets.newLinkedHashSet();
        }
        return ignorePages;
    }

}
