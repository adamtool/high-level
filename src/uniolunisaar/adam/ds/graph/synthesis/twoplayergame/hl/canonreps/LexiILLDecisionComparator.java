package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import java.util.Comparator;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;

/**
 *
 * @author Manuel Gieseking
 */
public class LexiILLDecisionComparator implements Comparator<ILLDecision> {

    @Override
    public int compare(ILLDecision dcs1, ILLDecision dcs2) {
        return dcs1.getIDChain().compareTo(dcs2.getIDChain());
    }

}
