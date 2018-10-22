package au.net.immortius.wardrobe.gwu;

import com.google.common.collect.Multiset;

import java.nio.file.Path;
import java.util.Set;

/**
 * Interface for matching screenshots of preview windows to a set of possible matches
 * @param <T> The type of the items to compare to
 */
public interface UnlockMatcher<T> {

    /**
     * @param screenshotRootPath The location of the preview window screenshots
     * @param possibleMatches The set of possible matches
     * @return A multiset of sets of matches. Each set of matches are the possible matches from a single
     * item in the screenshots, and the multiset provides a count of the occurrences of those matches
     */
    Multiset<Set<T>> matchIcons(Path screenshotRootPath, Set<T> possibleMatches);
}
