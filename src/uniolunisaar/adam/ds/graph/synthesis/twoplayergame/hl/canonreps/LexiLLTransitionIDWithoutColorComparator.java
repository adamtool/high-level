package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import java.util.Comparator;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class LexiLLTransitionIDWithoutColorComparator implements Comparator<Transition> {

    @Override
    public int compare(Transition t1, Transition t2) {
        return HL2PGConverter.getOrigID(t1).compareTo(HL2PGConverter.getOrigID(t2));
    }

}
