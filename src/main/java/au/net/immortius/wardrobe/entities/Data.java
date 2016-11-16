package au.net.immortius.wardrobe.entities;

/**
 *
 */
public class Data {

    public int id;
    public String name;
    public String icon;

    public String getIconName() {
        String[] pathParts = icon.split("/");
        return pathParts[pathParts.length - 2] + "-" + pathParts[pathParts.length - 1];
    }

}
