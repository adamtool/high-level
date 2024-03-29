package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLCommitmentSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLEnvDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLSysDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderLLCanon;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class CanonDecisionSet extends LLDecisionSet {

//    private final Symmetries syms;
//    public CanonDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game, Symmetries syms) {
//        super(decisions, mcut, bad, game);
//        this.syms = syms;
//    }
    public CanonDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {
        super(decisions, mcut, bad, game);
    }

    @Override
    public CanonDecisionSet createDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {

        SGGBuilderLLCanon.SaveMapping mapping = SGGBuilderLLCanon.getInstance().saveMapping;
        if (mapping == SGGBuilderLLCanon.SaveMapping.NONE) { // just make canonical
            Set<ILLDecision> canon;
            if (SGGBuilderLLCanon.getInstance().approach == SGGBuilderLLCanon.Approach.ORDERED_BY_LIST) {
                canon = makeCanonicalLists(decisions);
            } else if (SGGBuilderLLCanon.getInstance().approach == SGGBuilderLLCanon.Approach.ORDERED_BY_TREE) {
                canon = makeCanonicalTrees(decisions);
            } else if (SGGBuilderLLCanon.getInstance().approach == SGGBuilderLLCanon.Approach.APPROX) {
                canon = makeCanonicalApproximately(decisions);
            } else {
                // not implemented
                throw new UnsupportedOperationException("Approach " + SGGBuilderLLCanon.getInstance().approach + " is not yet implemented.");
            }
//            CanonDecisionSet dcs = new CanonDecisionSet(canon, mcut, bad, game, syms);
            CanonDecisionSet dcs = new CanonDecisionSet(canon, mcut, bad, game);
            return dcs;
        } else {
            // search if already a saved one exists
            CanonDecisionSet canonDCS = SGGBuilderLLCanon.getInstance().dcs2canon.get(decisions);
            if (canonDCS == null) { // nothing found
                // we still have to create the canonical
                Set<ILLDecision> canon;
                if (SGGBuilderLLCanon.getInstance().approach == SGGBuilderLLCanon.Approach.ORDERED_BY_LIST) {
                    canon = makeCanonicalLists(decisions);
                } else if (SGGBuilderLLCanon.getInstance().approach == SGGBuilderLLCanon.Approach.ORDERED_BY_TREE) {
                    canon = makeCanonicalTrees(decisions);
                } else if (SGGBuilderLLCanon.getInstance().approach == SGGBuilderLLCanon.Approach.APPROX) {
                    canon = makeCanonicalApproximately(decisions);
                } else {
                    // not implemented
                    throw new UnsupportedOperationException("Approach " + SGGBuilderLLCanon.getInstance().approach + " is not yet implemented.");
                }
//                canonDCS = new CanonDecisionSet(canon, mcut, bad, game, syms);
                canonDCS = new CanonDecisionSet(canon, mcut, bad, game);
                if (mapping == SGGBuilderLLCanon.SaveMapping.SOME) { // remember this pair
                    SGGBuilderLLCanon.getInstance().dcs2canon.put(decisions, canonDCS);
                } else { // calculate also all symmetric dcs to store also those combis     
                    SGGBuilderLLCanon.getInstance().dcs2canon.put(decisions, canonDCS);
//                    SymmetryIterator symIt = syms.iterator();
                    Iterator<Symmetry> symIt = SGGBuilderLLCanon.getInstance().getCurrentSymmetries().iterator();
                    // jump over identity
                    symIt.next();
                    for (Iterator<Symmetry> iterator = symIt; iterator.hasNext();) {
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

    private Set<ILLDecision> makeCanonicalApproximately(Set<ILLDecision> dcs) {
        Set<ILLDecision> ret = new HashSet<>();
        LexiILLDecisionComparatorWithoutColors comp = new LexiILLDecisionComparatorWithoutColors();
        List<ILLDecision> inputDCS = new ArrayList<>(dcs);
        // sort it 
        Collections.sort(inputDCS, comp);
        // make it minimal
        HLPetriGame hlgame = SGGBuilderLLCanon.getInstance().getCurrentHLGame();
        PetriGameWithTransits llgame = SGGBuilderLLCanon.getInstance().getCurrentLLGame();
        Map<Color, Color> colorMapping = new HashMap<>();
        Map<BasicColorClass, Integer> colorClassIdx = new HashMap<>(); // idx for the next color to use
        for (Iterator<ILLDecision> it = inputDCS.iterator(); it.hasNext();) {
            ILLDecision dc = it.next();
            // place
            String placeID = HL2PGConverter.getOrigID(dc.getPlace());
            List<Color> placeColors = HL2PGConverter.getColors(dc.getPlace());
            List<Color> newColors = getNewColors(hlgame, colorMapping, colorClassIdx, placeColors);
            Place newPlace = llgame.getPlace(HL2PGConverter.getPlaceID(placeID, newColors));
            if (dc.isEnvDecision()) {
                ret.add(new LLEnvDecision(llgame, newPlace));
            } else {
                // commitment sets
                if (dc.isTop()) {
                    ret.add(new LLSysDecision(llgame, newPlace, new LLCommitmentSet(llgame, true)));
                } else {
                    LLSysDecision sysDC = (LLSysDecision) dc;
                    Set<Transition> newTransitions = new HashSet<>();
                    for (Iterator<Transition> itTrans = sysDC.getCommitmentSetIterator(); itTrans.hasNext();) {
                        Transition t = itTrans.next();
                        String hlID = HL2PGConverter.getOrigID(t);
                        Valuation val = HL2PGConverter.getValuation(t);
                        Valuation newVal = new Valuation();
                        for (Map.Entry<Variable, Color> entry : val.entrySet()) {
                            Variable key = entry.getKey();
                            Color color = entry.getValue();
                            if (colorMapping.containsKey(color)) { // color already selected
                                newVal.put(key, colorMapping.get(color));
                            } else { // get the next color
                                BasicColorClass basicColorClass = hlgame.getBasicColorClass(color);
                                if (!colorClassIdx.containsKey(basicColorClass)) {
                                    colorClassIdx.put(basicColorClass, 0);
                                }
                                int idx = colorClassIdx.get(basicColorClass);
                                if (!basicColorClass.isOrdered()) {
                                    Color newColor = basicColorClass.getColor(idx);
                                    newVal.put(key, newColor);
                                    colorClassIdx.put(basicColorClass, ++idx);
                                    colorMapping.put(color, newColor);
                                } else { // the first fixed color of an ordered class fixed the complete class
                                    int oldIdx = basicColorClass.getIndex(color);
                                    List<Color> colors = basicColorClass.getColors();
                                    newVal.put(key, colors.get(0));
                                    for (int i = 0; i < colors.size(); i++) {
                                        Color newColor = colors.get(i);
                                        Color oldColor = colors.get(Math.floorMod((oldIdx + i), colors.size()));
                                        colorMapping.put(oldColor, newColor);
                                    }
                                }
                            }
                        }
                        Transition newTransition = llgame.getTransition(HL2PGConverter.calculateTransitionID(hlID, newVal));
                        newTransitions.add(newTransition);
                    }
                    ret.add(new LLSysDecision(llgame, newPlace, new LLCommitmentSet(llgame, newTransitions)));
                }
            }
        }
        return ret;
    }

    private List<Color> getNewColors(HLPetriGame hlgame, Map<Color, Color> colorMapping, Map<BasicColorClass, Integer> colorClassIdx, Collection<Color> colors) {
        List<Color> newColors = new ArrayList<>();
        for (Iterator<Color> itCol = colors.iterator(); itCol.hasNext();) {
            Color color = itCol.next();
            if (colorMapping.containsKey(color)) { // color already selected
                newColors.add(colorMapping.get(color));
            } else { // get the next color
                BasicColorClass basicColorClass = hlgame.getBasicColorClass(color);
                if (!colorClassIdx.containsKey(basicColorClass)) {
                    colorClassIdx.put(basicColorClass, 0);
                }
                int idx = colorClassIdx.get(basicColorClass);
                if (!basicColorClass.isOrdered()) {
                    Color newColor = basicColorClass.getColor(idx);
                    newColors.add(newColor);
                    colorClassIdx.put(basicColorClass, ++idx);
                    colorMapping.put(color, newColor);
                } else { // the first fixed color of an ordered class fixed the complete class
                    int oldIdx = basicColorClass.getIndex(color);
                    List<Color> basiColors = basicColorClass.getColors();
                    newColors.add(basiColors.get(0));
                    for (int i = 0; i < basiColors.size(); i++) {
                        Color newColor = basiColors.get(i);
                        Color oldColor = basiColors.get(Math.floorMod((oldIdx + i), basiColors.size()));
                        colorMapping.put(oldColor, newColor);
                    }
                }
            }
        }
        return newColors;
    }

    private Set<ILLDecision> makeCanonicalLists(Set<ILLDecision> dcs) {
        boolean skipSomeSyms = SGGBuilderLLCanon.getInstance().skipSomeSymmetries;
        LexiILLDecisionWithCommitmentComparator comp = new LexiILLDecisionWithCommitmentComparator();
        List<ILLDecision> inputDCS = new ArrayList<>(dcs);

        // sort it 
        Collections.sort(inputDCS, comp);
//        boolean notSorted = true;
        List<ILLDecision> smallest = inputDCS;
//        boolean calcSmallestID = true;
//        StringBuilder sbSmallestID = new StringBuilder();
//        SymmetryIterator symIt = syms.iterator();
//        SymmetryIterator symIt = SGGBuilderLLCanon.getInstance().getCurrentSymmetries().iterator();
        Iterator<Symmetry> symIt = SGGBuilderLLCanon.getInstance().getCurrentSymmetries().iterator();
        // jump over identity
        symIt.next();
        for (Iterator<Symmetry> iterator = symIt; iterator.hasNext();) {
            Symmetry sym = iterator.next();
            List<ILLDecision> back;
            if (skipSomeSyms) {
                back = calculateSmallestWhileApplyingSymmetry(sym, smallest, inputDCS, comp);
            } else {
                back = calculateSmallest(sym, smallest, inputDCS, comp);
            }
            if (back != null) {
                smallest = back;
            }
        }
        Set<ILLDecision> ret = new HashSet<>(smallest);
        return ret;
    }

    private List<ILLDecision> calculateSmallest(Symmetry sym, List<ILLDecision> smallest, List<ILLDecision> inputDCS, LexiILLDecisionWithCommitmentComparator comp) {
        // apply the symmetry
        List<ILLDecision> symDCS = new ArrayList<>();
        for (ILLDecision decision : inputDCS) {
            symDCS.add((ILLDecision) decision.apply(sym));
        }
        // sort the symmetric one
        Collections.sort(symDCS, comp);
        boolean smaller = checkSmaller(symDCS.iterator(), smallest.iterator(), comp, false);
        if (smaller) {
            return symDCS;
        }
        return null;

//        // this is only helpful when comparing general decision sets for symmetric they are of equal length
////            // when it is shorter it is smaller
////            if (symDCS.size() < smallest.size()) {
////                smallest = symDCS;
////                notSorted = true;
////                continue;
////            } else if (symDCS.size() > smallest.size()) { // when it is greater, it is NOT smaller
////                continue;
////            }
////            // if the size is equal, then sort
////            if (notSorted) {
////                // sort the smallest
////                Collections.sort(smallest, new LexiILLDecisionWithCommitmentComparator());
////                notSorted = false;
////            }
//        // sort the symmetric one
//        Collections.sort(symDCS, comp);
//        boolean smaller = false;
//        // old version comparing by symmetric
////            StringBuilder sbSym = new StringBuilder();
//////            StringBuilder sbSmallestID = new StringBuilder();
////            if (calcSmallestID) {
////                sbSmallestID.setLength(0);
////            }
//        for (int i = 0; i < symDCS.size(); i++) {
//            ILLDecision symDec = symDCS.get(i);
//            ILLDecision smallestDec = smallest.get(i);
//
////                // new version, check if it gets better
////                String symID = symDec.getIDChainByFirstSorting(); // here check just place!
////                String smallestID = smallestDec.getIDChainByFirstSorting();
////                int compare = symID.compareTo(smallestID);
//            int compare = comp.compare(symDec, smallestDec);
//            if (compare < 0) {
//                smaller = true;
//                break;
//            } else if (compare > 0) {
//                smaller = false;
//                break;
//            }
////                sbSym.append(symDec.getIDChainByFirstSorting()).append("|");
////                if (calcSmallestID) {
////                    sbSmallestID.append(smallestDec.getIDChainByFirstSorting()).append("|");
////                }
//        }
//        if (smaller) {
////            if (sbSym.toString().compareTo(sbSmallestID.toString()) < 0) {
//            return symDCS;
////                calcSmallestID = true;
////              notSorted = false;
//        }
////            } else if (calcSmallestID) {
////                calcSmallestID = false;
////            }
//        return null;
    }

    private List<ILLDecision> calculateSmallestWhileApplyingSymmetry(Symmetry sym, List<ILLDecision> smallest, List<ILLDecision> inputDCS, LexiILLDecisionWithCommitmentComparator comp) {
        List<ILLDecision> symDCS = new ArrayList<>();
        List<ILLDecision> block = new ArrayList<>();
        Iterator<ILLDecision> smallestPosition = smallest.iterator();
        String hlIDPlace = null;
        for (Iterator<ILLDecision> iterator = inputDCS.iterator(); iterator.hasNext();) {
            // apply sym
            ILLDecision symDC = (ILLDecision) iterator.next().apply(sym);

            String hlIDCurrentPlace = HL2PGConverter.getOrigID(symDC.getPlace());
            if (hlIDPlace == null) {
                hlIDPlace = hlIDCurrentPlace;
                block.add(symDC);
            } else if (hlIDPlace.equals(hlIDCurrentPlace)) {// still the same hl place
                block.add(symDC); // just add it to the current block
            } else {
                Collections.sort(block, comp);
                Iterator<ILLDecision> blockIT = block.iterator();
//                boolean smaller = checkSmaller(blockIT, smallestPosition, comp, false);
                int compare = checkSmaller(blockIT, smallestPosition, comp);
                if (compare < 0) { // we already found the symmetry for the smallest
                    symDCS.addAll(block);
                    block = new ArrayList<>();
                    block.add(symDC);
                    while (iterator.hasNext()) {
                        ILLDecision symdc2 = (ILLDecision) iterator.next().apply(sym);
                        block.add(symdc2);
                    }
                    Collections.sort(block, comp);
                    symDCS.addAll(block);
                    return symDCS;
                } else if (compare > 0) {
                    return null;
                } else {
                    // if break was used fast forward the smallest iterator to the next block           
                    while (blockIT.hasNext()) {
                        blockIT.next();
                        smallestPosition.next();
                    }
                    symDCS.addAll(block);
                    block = new ArrayList<>();
                    block.add(symDC);
                    hlIDPlace = hlIDCurrentPlace;
                }
            }
        }
        Collections.sort(block, comp);
        boolean smaller = checkSmaller(block.iterator(), smallestPosition, comp, false);
        if (smaller) {
            symDCS.addAll(block);
        } else {
            return null;
        }
        return symDCS;
    }

    private Set<ILLDecision> makeCanonicalTrees(Set<ILLDecision> dcs) {
        boolean skipSomeSyms = SGGBuilderLLCanon.getInstance().skipSomeSymmetries;
        LexiILLDecisionWithCommitmentComparator comp = new LexiILLDecisionWithCommitmentComparator();
        TreeSet<ILLDecision> inputDCS = new TreeSet<>(comp);
        inputDCS.addAll(dcs);

//        boolean notSorted = true;
        TreeSet<ILLDecision> smallest = inputDCS;
//        boolean calcSmallestID = true;
//        StringBuilder sbSmallestID = new StringBuilder();
//        SymmetryIterator symIt = syms.iterator();
//        SymmetryIterator symIt = SGGBuilderLLCanon.getInstance().getCurrentSymmetries().iterator();
        Iterator<Symmetry> symIt = SGGBuilderLLCanon.getInstance().getCurrentSymmetries().iterator();
        // jump over identity
        symIt.next();
        for (Iterator<Symmetry> iterator = symIt; iterator.hasNext();) {
            Symmetry sym = iterator.next();
            TreeSet<ILLDecision> back;
            if (skipSomeSyms) {
                back = calculateSmallestWhileApplyingSymmetry(sym, smallest, inputDCS, comp);
            } else {
                back = calculateSmallest(sym, smallest, inputDCS, comp);
            }
            if (back != null) {
                smallest = back;
            }
        }
        return new HashSet<>(smallest);
    }

    private TreeSet<ILLDecision> calculateSmallestWhileApplyingSymmetry(Symmetry sym, TreeSet<ILLDecision> smallest, TreeSet<ILLDecision> inputDCS, LexiILLDecisionWithCommitmentComparator comp) {
        TreeSet<ILLDecision> symDCS = new TreeSet<>(comp);
        TreeSet<ILLDecision> block = new TreeSet<>(comp);
        Iterator<ILLDecision> smallestPosition = smallest.iterator();
        String hlIDPlace = null;
        for (Iterator<ILLDecision> iterator = inputDCS.iterator(); iterator.hasNext();) {
            // apply sym
            ILLDecision symDC = (ILLDecision) iterator.next().apply(sym);
            String hlIDCurrentPlace = HL2PGConverter.getOrigID(symDC.getPlace());
            if (hlIDPlace == null) {
                hlIDPlace = hlIDCurrentPlace;
                block.add(symDC);
            } else if (hlIDPlace.equals(hlIDCurrentPlace)) {// still the same hl place            
                block.add(symDC); // just add it to the current block
            } else {
                Iterator<ILLDecision> blockIT = block.iterator();
//                boolean smaller = checkSmaller(blockIT, smallestPosition, comp, true);
                int smaller = checkSmaller(blockIT, smallestPosition, comp);
                if (smaller < 0) {
                    symDCS.addAll(block);
                    symDCS.add(symDC);
                    while (iterator.hasNext()) {
                        ILLDecision symdc2 = (ILLDecision) iterator.next().apply(sym);
                        symDCS.add(symdc2);
                    }
                    return symDCS;
                } else if (smaller > 0) {
                    return null;
                } else {
                    // if break was used fast forward the smallest iterator to the next block           
                    while (blockIT.hasNext()) {
                        blockIT.next();
                        smallestPosition.next();
                    }
                    symDCS.addAll(block);
                    block = new TreeSet<>(comp);
                    block.add(symDC);
                    hlIDPlace = hlIDCurrentPlace;
                }
            }
        }
        boolean smaller = checkSmaller(block.iterator(), smallestPosition, comp, true);
        if (smaller) {
            symDCS.addAll(block);
        } else {
            return null;
        }
        return symDCS;
    }

    private TreeSet<ILLDecision> calculateSmallest(Symmetry sym, TreeSet<ILLDecision> smallest, TreeSet<ILLDecision> inputDCS, LexiILLDecisionWithCommitmentComparator comp) {
        TreeSet<ILLDecision> symDCS = new TreeSet<>(comp);
        // apply the symmetry
        for (ILLDecision decision : inputDCS) {
            symDCS.add((ILLDecision) decision.apply(sym));
        }
        boolean smaller = checkSmaller(symDCS.iterator(), smallest.iterator(), comp, false);
        if (smaller) {
            return symDCS;
        }
        return null;
//        // this is only helpful when comparing general decision sets for symmetric they are of equal length
////            // when it is shorter it is smaller
////            if (symDCS.size() < smallest.size()) {
////                smallest = symDCS;
////                notSorted = true;
////                continue;
////            } else if (symDCS.size() > smallest.size()) { // when it is greater, it is NOT smaller
////                continue;
////            }
////            // if the size is equal, then sort
////            if (notSorted) {
////                // sort the smallest
////                Collections.sort(smallest, new LexiILLDecisionWithCommitmentComparator());
////                notSorted = false;
////            }
//        boolean smaller = false;
////            StringBuilder sbSym = new StringBuilder();
//////            StringBuilder sbSmallestID = new StringBuilder();
////            if (calcSmallestID) {
////                sbSmallestID.setLength(0);
////            }
//        Iterator<ILLDecision> itSym = symDCS.iterator();
//        Iterator<ILLDecision> itSmallest = smallest.iterator();
//        while (itSym.hasNext()) {
//            ILLDecision symDec = itSym.next();
//            ILLDecision smallestDec = itSmallest.next();
////                // new version, check if it gets better
////                String symID = symDec.getIDChainByFirstSorting();
////                String smallestID = smallestDec.getIDChainByFirstSorting();
////                int compare = symID.compareTo(smallestID);
//            int compare = comp.compare(symDec, smallestDec);
//            if (compare < 0) {
//                smaller = true;
//                break;
//            } else if (compare > 0) {
//                smaller = false;
//                break;
//            }
////                sbSym.append(symDec.getIDChainByFirstSorting()).append("|");
////                if (calcSmallestID) {
////                    sbSmallestID.append(smallestDec.getIDChainByFirstSorting()).append("|");
////                }
//        }
//        if (smaller) {
////            if (sbSym.toString().compareTo(sbSmallestID.toString()) < 0) {
//            return symDCS;
////                calcSmallestID = true;
////              notSorted = false;
//        }
////            } else if (calcSmallestID) {
////                calcSmallestID = false;
////            }
//        return null;
    }

    /**
     * Lists must be sorted.
     *
     * @param itSym
     * @param itSmallest
     * @param comp
     * @return
     */
    private int checkSmaller(Iterator<ILLDecision> itSym, Iterator<ILLDecision> itSmallest, LexiILLDecisionWithCommitmentComparator comp) {
        while (itSym.hasNext()) {
            ILLDecision symDec = itSym.next();
            ILLDecision smallestDec = itSmallest.next();
            int compare = comp.compare(symDec, smallestDec);
            if (compare < 0) {
                return -1;
            } else if (compare > 0) {
                return 1;
            }
        }
        return 0;
    }

    private boolean checkSmaller(Iterator<ILLDecision> itSym, Iterator<ILLDecision> itSmallest, LexiILLDecisionWithCommitmentComparator comp, boolean andEqual) {
        boolean smaller = andEqual;
        while (itSym.hasNext()) {
            ILLDecision symDec = itSym.next();
            ILLDecision smallestDec = itSmallest.next();
            int compare = comp.compare(symDec, smallestDec);
            if (compare < 0) {
                smaller = true;
                break;
            } else if (compare > 0) {
                smaller = false;
                break;
            }
        }
        return smaller;
    }

    /// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
    // these methods could be used, when some datastructure is used which
    // preserves an order (e.g. List, vs. HashSet). Then sorting would 
    // have an effect.
    public CanonDecisionSet createDecisionSetWhenDCSEnsureAnOrder(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {
//        CanonDecisionSet dcs = new CanonDecisionSet(decisions, mcut, bad, game, syms);
        CanonDecisionSet dcs = new CanonDecisionSet(decisions, mcut, bad, game);
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
//                    for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
                    for (Iterator<Symmetry> iterator = SGGBuilderLLCanon.getInstance().getCurrentSymmetries().iterator(); iterator.hasNext();) {
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
//        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
        for (Iterator<Symmetry> iterator = SGGBuilderLLCanon.getInstance().getCurrentSymmetries().iterator(); iterator.hasNext();) {
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
