package au.net.immortius.wardrobe.vendors;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Objects;

/**
 * Class for easing working with wiki urls. Strips fragments.
 */
public class WikiUrl {

    private String baseUrl;
    private String url;
    private String filename;
    private boolean userPage = false;

    /**
     * Creates a wiki url
     *
     * @param relativeUrl The relative url
     */
    public WikiUrl(String relativeUrl) {
        this(relativeUrl, "");
    }

    /**
     * Creates a subsequent page url for a category page
     *
     * @param relativeUrl The relative url of the category page
     * @param pageFrom    The entry to start the page from
     */
    public WikiUrl(String relativeUrl, String pageFrom) {
        this.baseUrl = relativeUrl;
        if (relativeUrl.indexOf('#') != -1) {
            this.baseUrl = relativeUrl.substring(0, relativeUrl.indexOf('#'));
        }
        this.url = this.baseUrl;
        if (!pageFrom.isEmpty()) {
            this.url += "?pagefrom=" + pageFrom.replace(" ", "+");
        }

        if (relativeUrl.contains("/User:")) {
            userPage = true;
        }

        if (relativeUrl.lastIndexOf(":") != -1) {
            filename = baseUrl.substring(relativeUrl.lastIndexOf(":") + 1) + "-" + pageFrom + ".html";
        } else {
            filename = baseUrl.substring(relativeUrl.lastIndexOf("/") + 1) + "-" + pageFrom + ".html";
        }
    }

    /**
     * @return The complete url string
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return The base url string, dropping any pagination
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * @return The filename to use for caching the page
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return Extract a displayable page name from a wiki url
     */
    public String getDisplayName() {
        try {
            String namePortion = URLDecoder.decode(baseUrl, "UTF-8").substring(6).replaceAll("_", " ");
            if (namePortion.contains("/")) {
                namePortion = namePortion.substring(0, namePortion.indexOf("/"));
            }
            if (namePortion.contains("(")) {
                namePortion = namePortion.substring(0, namePortion.indexOf("("));
            }
            return namePortion.trim();
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Why does your java not support UTF-8???");
        }
    }

    /**
     * @return Whether the url is for a user page
     */
    public boolean isUserPage() {
        return userPage;
    }

    @Override
    public String toString() {
        return getUrl();
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof WikiUrl) {
            return Objects.equals(url, ((WikiUrl) obj).url);
        }
        return false;
    }
}
