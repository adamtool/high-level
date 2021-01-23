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
        // this is a problem because the comparator is then most likely also used to check
        // whether two OrderedDecisionSets are equal, and this results in equal states which are not equal
//        return dcs1.getPlace().getId().compareTo(dcs2.getPlace().getId()); 
    }

}
