package au.net.immortius.wardrobe.legacy.entities;

import com.google.common.base.Strings;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 *
 */
public class IconConfig {
    public String id;
    private String dataId;
    private String iconId;
    public String url;
    private Set<Integer> ignoreIds;
    private Set<Integer> iconSharingIds;
    private Set<Integer> joinedIds;
    private Class<? extends Data> javaClass;
    private Predicate<Data> dataFilter;
    private Function<List<Data>, List<Data>> matchFilter;
    private int matchThreshold;

    public String getIconId() {
        if (!Strings.isNullOrEmpty(iconId)) {
            return iconId;
        }
        return id + "-icon";
    }

    public String getDataId() {
        if (Strings.isNullOrEmpty(dataId)) {
            return id;
        }
        return dataId;
    }

    public Class<? extends Data> getJavaClass() {
        if (javaClass == null) {
            return Data.class;
        }
        return javaClass;
    }

    public Predicate<Data> getDataFilter() {
        if (dataFilter != null) {
            return dataFilter;
        }
        return (x) -> true;
    }

    public Function<List<Data>, List<Data>> getMatchFilter() {
        if (matchFilter != null) {
            return matchFilter;
        }
        return (x) -> x;
    }

    public int getMatchThreshold() {
        if (matchThreshold != 0) {
            return matchThreshold;
        }
        return 100;
    }

    public Set<Integer> getIgnoreIds() {
        if (ignoreIds == null) {
            return Collections.emptySet();
        }
        return ignoreIds;
    }

    public Set<Integer> getIconSharingIds() {
        if (iconSharingIds == null) {
            return Collections.emptySet();
        }
        return iconSharingIds;
    }

    public Set<Integer> getJoinedIds() {
        if (joinedIds == null) {
            return Collections.emptySet();
        }
        return joinedIds;
    }
}
