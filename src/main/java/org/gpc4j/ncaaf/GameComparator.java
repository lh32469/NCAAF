package org.gpc4j.ncaaf;

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.util.Comparator;
import org.gpc4j.ncaaf.jaxb.Game;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Lyle T Harris
 */
public class GameComparator implements Comparator<Game> {

    final static private org.slf4j.Logger LOG
            = LoggerFactory.getLogger(GameComparator.class);


    @Override
    public int compare(Game g1, Game g2) {

        // Any Game without a date hasn't been played yet
        // so gets place after any Game with a date.
        if (!Strings.isNullOrEmpty(g1.getDate())
                && Strings.isNullOrEmpty(g2.getDate())) {
            return -1;
        } else if (Strings.isNullOrEmpty(g1.getDate())
                && !Strings.isNullOrEmpty(g2.getDate())) {

            return 1;
        } else if (!Strings.isNullOrEmpty(g1.getDate())
                && !Strings.isNullOrEmpty(g2.getDate())) {
            LocalDateTime d1 = LocalDateTime.parse(g1.getDate());
            LocalDateTime d2 = LocalDateTime.parse(g2.getDate());

            return d1.compareTo(d2);
        } else {
            return 0;
        }

    }


}
