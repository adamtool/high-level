package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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

        SGGBuilderLLCanon.SaveMapping mapping = SGGBuilderLLCanon.getInstance().saveMapping;
        if (mapping == SGGBuilderLLCanon.SaveMapping.NONE) { // just make canonical
            Set<ILLDecision> canon = makeCanonicalLists(decisions);
            CanonDecisionSet dcs = new CanonDecisionSet(canon, mcut, bad, game, syms);
            return dcs;
        } else {
            // search if already a saved one exists
            CanonDecisionSet canonDCS = SGGBuilderLLCanon.getInstance().dcs2canon.get(decisions);
            if (canonDCS == null) { // nothing found
                // we still have to create the canonical
                Set<ILLDecision> canon = makeCanonicalLists(decisions);
                canonDCS = new CanonDecisionSet(canon, mcut, bad, game, syms);
                if (mapping == SGGBuilderLLCanon.SaveMapping.SOME) { // remember this pair
                    SGGBuilderLLCanon.getInstance().dcs2canon.put(decisions, canonDCS);
                } else { // calculate also all symmetric dcs to store also those combis
                    for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
                        Symmetry sym = iterator.next();
                        // apply the symmetry
                        Set<ILLDecision> symDCS = new HashSet<>();
                        for (ILLDecision decision : decisions) {
                            symDCS.add((ILLDecision) decision.apply(sym));
                        }
                        SGGBuilderLLCanon.getInstance().dcs2canon.put(symDCS, canonDCS);
                    }
                }
            }
            return canonDCS;
        }
    }

    private Set<ILLDecision> makeCanonicalLists(Set<ILLDecision> dcs) {
        List<ILLDecision> inputDCS = new ArrayList<>(dcs);

        boolean notSorted = true;
        List<ILLDecision> smallest = inputDCS;
        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
            Symmetry sym = iterator.next();
            // apply the symmetry
            List<ILLDecision> symDCS = new ArrayList<>();
            for (ILLDecision decision : inputDCS) {
                symDCS.add((ILLDecision) decision.apply(sym));
            }
//            // when it is shorter it is smaller
//            if (symDCS.size() < smallest.size()) {
//                smallest = symDCS;
//                notSorted = true;
//                continue;
//            } else if (symDCS.size() > smallest.size()) { // when it is greater, it is NOT smaller
//                continue;
//            }
            // if the size is equal, then sort
            if (notSorted) {
                // sort the smallest
                Collections.sort(smallest, new LexiILLDecisionWithCommitmentComparator());
                notSorted = false;
            }
            // sort the symmetric one
            Collections.sort(symDCS, new LexiILLDecisionWithCommitmentComparator());
            for (int i = 0; i < symDCS.size(); i++) {
                ILLDecision symDec = symDCS.get(i);
                ILLDecision smallestDec = smallest.get(i);
                if (symDec.getIDChainByFirstSorting().compareTo(smallestDec.getIDChainByFirstSorting()) < 0) {
                    smallest = symDCS;
                    notSorted = false;
                }
            }
        }
        return new HashSet<>(smallest);
    }

//    private Set<ILLDecision> makeCanonicalTrees(Set<ILLDecision> dcs) {
//        TreeSet<ILLDecision> sortedDCS = new TreeSet<>(new LexiILLDecisionComparator());
//        sortedDCS.addAll(dcs);
//        Set<ILLDecision> smallest = dcs;
//        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
//            Symmetry sym = iterator.next();
//            // apply the symmetry
//            List<ILLDecision> decs = new ArrayList<>();
//            for (ILLDecision decision : decisions) {
//                decs.add((ILLDecision) decision.apply(sym));
//            }
//            CanonDecisionSet symDcs = (CanonDecisionSet) dcs.apply(sym); // is ensured due overwritten createDecisionSet 
////            // symDcs.sort();
////            if (symDcs.getIDChain().compareTo(smallest.getIDChain()) < 0) {
////                smallest = symDcs;
////            }
//        }
//        return smallest;
//    }

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
//                    SGGBuilderLLCanon.getInstance().dcs2canon.put(dcs, canon);
                } else {
                    for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
                        Symmetry sym = iterator.next();
                        CanonDecisionSet symDcs = (CanonDecisionSet) dcs.apply(sym); // is ensured due overwritten createDecisionSet 
//                        SGGBuilderLLCanon.getInstance().dcs2canon.put(symDcs, canon);
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
