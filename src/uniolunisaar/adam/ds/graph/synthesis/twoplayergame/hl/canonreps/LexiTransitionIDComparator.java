package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import java.util.Comparator;
import uniol.apt.adt.pn.Transition;

/**
 *
 * @author Manuel Gieseking
 */
public class LexiTransitionIDComparator implements Comparator<Transition> {

    @Override
    public int compare(Transition t1, Transition t2) {
        return t1.getId().compareTo(t2.getId());
    }

}
