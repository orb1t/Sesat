/* Copyright (2006-2007) Schibsted Søk AS
 * This file is part of SESAT.
 * You can use, redistribute, and/or modify it, under the terms of the SESAT License.
 * You should have received a copy of the SESAT License along with this program.  
 * If not, see https://dev.sesat.no/confluence/display/SESAT/SESAT+License

 */
package no.sesat.searchportal.result;

import java.io.Serializable;
import java.util.Comparator;

/**
 * @author <a href="mailto:magnus.eklund@schibsted.no">Magnus Eklund</a>
 * @version <tt>$Revision$</tt>
 */
public final class Modifier implements Comparable<Modifier>, Serializable {
    
    private int count = 0;

    private final String name;
    private final Navigator navigator;

    private static final Comparator<Modifier> HINT_PRIO_COMPARATOR = new Comparator<Modifier>() {

        public int compare(final Modifier m1, final Modifier m2) {
            
            return 0; //FIXME restore
//            if (m1.getNavigationHint() == null || m2.getNavigationHint() == null) {
//                return 0;
//            }
//
//            int p1 = m1.getNavigationHint().getPriority();
//            int p2 = m2.getNavigationHint().getPriority();
//             
//             if (p1 == p2) {
//                 return 0;
//             }
//             
//             return p1 > p2 ? 1 : -1;
        }
    };

    public Modifier(final String name, final int count, final Navigator navigator) {
        this.name = name;
        this.count = count;
        this.navigator = navigator;
    }

    public Modifier(final String name, final Navigator navigator) {
        this.name = name;
        this.navigator = navigator;
    }

    
    public void addCount(final int count) {
        this.count += count;
    }

    public void subtractCount(final int count) {
        this.count -= count;
    }    
    
    public Navigator getNavigator() {
        return navigator;
    }

    public int getCount() {
        return count;
    }

    public String getName() {
        return name;
    }

    public int compareTo(final Modifier other) {

        return Integer.valueOf(other.getCount()).compareTo(getCount());
    }

    @Override
    public String toString() {
        return name + '(' + getCount() + ')';
    }

    public static Comparator<Modifier> getHintPriorityComparator() {
        return HINT_PRIO_COMPARATOR;
    }
}