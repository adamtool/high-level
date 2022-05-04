package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import java.util.Comparator;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.SysDecision;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;
import uniolunisaar.adam.util.AdamPGWTExtensions;
import uniolunisaar.adam.util.ExtensionManagement;

/**
 *
 * @author Manuel Gieseking
 */
public class LexiILLDecisionComparatorWithoutColors implements Comparator<ILLDecision> {

    @Override
    public int compare(ILLDecision dc1, ILLDecision dc2) {
        // compare the hl place ids         
        Place p1 = dc1.getPlace();
        String p1ID = HL2PGConverter.getOrigID(p1);
        String p2ID = HL2PGConverter.getOrigID(dc2.getPlace());
        int compare = p1ID.compareTo(p2ID);
        if (compare < 0) {
            return -1;
        } else if (compare > 0) {
            return 1;
        }
        // ATTENTION: this only works for correct decision sets (so no env and sys with the same id)
        if (ExtensionManagement.getInstance().hasExtension(p1, AdamPGWTExtensions.env)) {
            return 0;
        }

        // compare the commitment sets
        // check tops
        if (dc1.isTop() && !dc2.isTop()) {
            return -1;
        } else if (!dc1.isTop() && dc2.isTop()) {
            return 1;
        } else if (dc1.isTop() && dc2.isTop()) {
            return 0;
        }
        // check the sizes
        SysDecision sysDC1 = (SysDecision) dc1;
        SysDecision sysDC2 = (SysDecision) dc2;
        if (sysDC1.getCommitmentSetSize() < sysDC2.getCommitmentSetSize()) {
            return -1;
        } else if (sysDC2.getCommitmentSetSize() < sysDC1.getCommitmentSetSize()) {
            return 1;
        }    
        // now compare the ordered transition ids without the colors
        return sysDC1.getCommitmentSetIDChainByFirstSorting(true).compareTo(sysDC2.getCommitmentSetIDChainByFirstSorting(true));        
    }

}
