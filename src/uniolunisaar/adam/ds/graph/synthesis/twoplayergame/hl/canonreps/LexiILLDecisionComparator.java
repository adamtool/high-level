package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import java.util.Comparator;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.SysDecision;
import uniolunisaar.adam.util.AdamExtensions;

/**
 *
 * @author Manuel Gieseking
 */
public class LexiILLDecisionComparator implements Comparator<ILLDecision> {

    @Override
    public int compare(ILLDecision dcs1, ILLDecision dcs2) {
        Place p1 = dcs1.getPlace();
        Place p2 = dcs2.getPlace();
        int compare = p1.getId().compareTo(p2.getId());
        if (compare < 0) {
            return -1;
        } else if (compare > 0) {
            return 1;
        }
        // ATTENTION: this only works for correct decision sets (so no env and sys with the same id)
        if (p1.hasExtension(AdamExtensions.env.name())) {
            return 0;
        }

        // ATTENTION: this only works for ILLDecisions with CommitmentSet as 
        // commitment set. Just to keep it for our cases fast!
        return ((SysDecision) dcs1).getIDChain().compareTo(((SysDecision) dcs2).getIDChain());
        // old version:
//        return dcs1.getIDChain().compareTo(dcs2.getIDChain());
        // this is a problem because the comparator is then most likely also used to check
        // whether two OrderedDecisionSets are equal, and this results in equal states which are not equal
//        return dcs1.getPlace().getId().compareTo(dcs2.getPlace().getId()); 
    }

}
