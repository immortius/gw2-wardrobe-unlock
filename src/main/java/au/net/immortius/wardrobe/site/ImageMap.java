package au.net.immortius.wardrobe.site;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class ImageMap implements Iterable<IconDetails> {
    private String name;
    private String image;
    private Set<String> contents = Sets.newLinkedHashSet();
    private int mapSize = 2048;
    private int iconSize = 64;

    public ImageMap(String name) {
        this.name = name;
    }

    public ImageMap(String name, String image, Collection<String> contents) {
        this.name = name;
        this.image = image;
        this.contents.addAll(contents);
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return this.image;
    }

    public Set<String> getContents() {
        return ImmutableSet.copyOf(contents);
    }

    @Override
    public Iterator<IconDetails> iterator() {
        return new Iterator<IconDetails>() {
            private int index = 0;
            private Iterator<String> contentIterator = contents.iterator();
            private int dim = mapSize / iconSize;

            @Override
            public boolean hasNext() {
                return contentIterator.hasNext();
            }

            @Override
            public IconDetails next() {
                int yOffset = (index / dim) * iconSize;
                int xOffset = (index % dim) * iconSize;
                IconDetails next = new IconDetails(name, xOffset, yOffset, contentIterator.next());
                index++;
                return next;
            }
        };
    }
}
