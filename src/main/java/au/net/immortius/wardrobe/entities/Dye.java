package au.net.immortius.wardrobe.entities;

import java.util.List;

/**
 *
 */
public class Dye {
    public int id;
    public String name;
    public int[] base_rgb;
    public DyeDetail cloth;
    public DyeDetail leather;
    public DyeDetail metal;
    public int item;
    public List<String> categories;
}
