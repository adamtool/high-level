package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import java.util.Set;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLDecisionSet;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.SymmetryIterator;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderLLCanon;

/**
 *
 * @author Manuel Gieseking
 */
public class CanonDecisionSet extends LLDecisionSet {

    private final Symmetries syms;

    public CanonDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game, Symmetries syms) {
        super(decisions, mcut, bad, game);
        this.syms = syms;
    }

    @Override
    public CanonDecisionSet createDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {

        return null;
//        CanonDecisionSet dcs = new CanonDecisionSet(decisions, mcut, bad, game, syms);
//        SGGBuilderLLCanon.SaveMapping mapping = SGGBuilderLLCanon.getInstance().saveMapping;
//        if (mapping == SGGBuilderLLCanon.SaveMapping.NONE) {
//            return makeCanonical(dcs);
//        } else {
//            CanonDecisionSet canon = SGGBuilderLLCanon.getInstance().dcs2canon.get(dcs);
//            if (canon == null) {
//                canon = makeCanonical(dcs);
//                if (mapping == SGGBuilderLLCanon.SaveMapping.SOME) {
//                    SGGBuilderLLCanon.getInstance().dcs2canon.put(dcs, canon);
//                } else {
//                    for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
//                        Symmetry sym = iterator.next();
//                        CanonDecisionSet symDcs = (CanonDecisionSet) dcs.apply(sym); // is ensured due overwritten createDecisionSet 
//                        SGGBuilderLLCanon.getInstance().dcs2canon.put(symDcs, canon);
//                    }
//                }
//            }
//            return canon;
//        }
    }

    private CanonDecisionSet makeCanonical(Set<ILLDecision> dcs) {
        // dcs.sort();
//        Set<ILLDecision> smallest = dcs;
//        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
//            Symmetry sym = iterator.next();
//            CanonDecisionSet symDcs = (CanonDecisionSet) dcs.apply(sym); // is ensured due overwritten createDecisionSet 
//            // symDcs.sort();
//            if (symDcs.getIDChain().compareTo(smallest.getIDChain()) < 0) {
//                smallest = symDcs;
//            }
//        }
//        return smallest;
        return null;
    }

    // these methods could be used, when some datastructure is used which
    // preserves an order (e.g. List, vs. HashSet). Then sorting would 
    // have an effect.
    public CanonDecisionSet createDecisionSetWhenDCSEnsureAnOrder(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {
        CanonDecisionSet dcs = new CanonDecisionSet(decisions, mcut, bad, game, syms);
        SGGBuilderLLCanon.SaveMapping mapping = SGGBuilderLLCanon.getInstance().saveMapping;
        if (mapping == SGGBuilderLLCanon.SaveMapping.NONE) {
            return makeCanonicalWhenDCSEnsureAnOrder(dcs);
        } else {
            CanonDecisionSet canon = SGGBuilderLLCanon.getInstance().dcs2canon.get(dcs);
            if (canon == null) {
                canon = makeCanonicalWhenDCSEnsureAnOrder(dcs);
                if (mapping == SGGBuilderLLCanon.SaveMapping.SOME) {
                    SGGBuilderLLCanon.getInstance().dcs2canon.put(dcs, canon);
                } else {
                    for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
                        Symmetry sym = iterator.next();
                        CanonDecisionSet symDcs = (CanonDecisionSet) dcs.apply(sym); // is ensured due overwritten createDecisionSet 
                        SGGBuilderLLCanon.getInstance().dcs2canon.put(symDcs, canon);
                    }
                }
            }
            return canon;
        }
    }

    private CanonDecisionSet makeCanonicalWhenDCSEnsureAnOrder(CanonDecisionSet dcs) {
        // dcs.sort();
        CanonDecisionSet smallest = dcs;
        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
            Symmetry sym = iterator.next();
            CanonDecisionSet symDcs = (CanonDecisionSet) dcs.apply(sym); // is ensured due overwritten createDecisionSet 
            // symDcs.sort();
            if (symDcs.getIDChain().compareTo(smallest.getIDChain()) < 0) {
                smallest = symDcs;
            }
        }
        return smallest;
    }

}
