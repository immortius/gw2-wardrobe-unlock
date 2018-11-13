package au.net.immortius.wardrobe.config;

import java.util.Set;

/**
 * A generic entity for named groups of ids, used for defining data to drive site generation where it is not otherwise
 * available (i.e. unlock groupings and acquisition methods)
 */
public class Grouping {
    public String name;
    public Set<Integer> contents;
}
