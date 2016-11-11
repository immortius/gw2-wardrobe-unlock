package entities;

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
    private String iconId;
    public String url;
    private Set<Integer> ignoreIds;
    private Set<Integer> iconSharingIds;
    private Set<Integer> joinedIds;
    private Class<? extends Common> javaClass;
    private Predicate<Common> dataFilter;
    private Function<List<Common>, List<Common>> matchFilter;
    private int matchThreshold;

    public String getIconId() {
        if (!Strings.isNullOrEmpty(iconId)) {
            return iconId;
        }
        return id + "-icons";
    }

    public Class<? extends Common> getJavaClass() {
        if (javaClass == null) {
            return Common.class;
        }
        return javaClass;
    }

    public Predicate<Common> getDataFilter() {
        if (dataFilter != null) {
            return dataFilter;
        }
        return (x) -> true;
    }

    public Function<List<Common>, List<Common>> getMatchFilter() {
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
