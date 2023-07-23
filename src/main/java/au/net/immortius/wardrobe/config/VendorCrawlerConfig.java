package au.net.immortius.wardrobe.config;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.util.List;
import java.util.Set;

public class VendorCrawlerConfig {
    public String rootUrl;

    public Set<String> skinTypes;

    public Set<String> noveltyTypes;

    private List<String> categoryPages;

    private List<String> vendorPages;

    private Set<String> ignorePages;

    private Set<String> miniatureTypes;

    private Set<String> gliderTypes;

    private Set<String> mailCarrierTypes;

    public String miniId;

    public String noveltyId;

    public String gliderId;

    public String mailCarrierId;

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

    public Set<String> getMiniatureTypes() {
        if (miniatureTypes == null) {
            miniatureTypes = Sets.newLinkedHashSet();
        }
        return miniatureTypes;
    }

    public Set<String> getGliderTypes() {
        if (gliderTypes == null) {
            gliderTypes = Sets.newLinkedHashSet();
        }
        return gliderTypes;
    }

    public Set<String> getMailCarrierTypes() {
        if (mailCarrierTypes == null) {
            mailCarrierTypes = Sets.newLinkedHashSet();
        }
        return mailCarrierTypes;
    }

    public Set<String> getNoveltyTypes() {
        if (noveltyTypes == null) {
            noveltyTypes = Sets.newLinkedHashSet();
        }
        return noveltyTypes;
    }
}
