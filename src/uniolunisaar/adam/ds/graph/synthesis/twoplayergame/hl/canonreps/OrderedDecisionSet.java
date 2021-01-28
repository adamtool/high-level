package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.tools.CartesianProduct;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLDecisionSet;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.SymmetryIterator;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderLLCanon;

/**
 *
 * This approach uses an order set to store the elements. The class
 * CanonDecisionSet however uses the normal structure and sort it when needed.
 *
 * @author Manuel Gieseking
 */
public class OrderedDecisionSet extends LLDecisionSet {

    private final Symmetries syms;

    public OrderedDecisionSet(TreeSet<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game, Symmetries syms) {
        super(decisions, mcut, bad, game);
        this.syms = syms;
    }

    public OrderedDecisionSet createDecisionSet(TreeSet<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game, Symmetries syms) {
        OrderedDecisionSet dcs = new OrderedDecisionSet(decisions, mcut, bad, game, syms);
        SGGBuilderLLCanon.SaveMapping mapping = SGGBuilderLLCanon.getInstance().saveMapping;
        if (mapping == SGGBuilderLLCanon.SaveMapping.NONE) {
            return getCanonical(dcs);
        } else {
            OrderedDecisionSet canon = SGGBuilderLLCanon.getInstance().dcsOrdered2canon.get(dcs);
            if (canon == null) {
                canon = getCanonical(dcs);
                if (mapping == SGGBuilderLLCanon.SaveMapping.SOME) {
                    SGGBuilderLLCanon.getInstance().dcsOrdered2canon.put(dcs, canon);
                } else {
                    for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
                        Symmetry sym = iterator.next();
                        OrderedDecisionSet symDcs = dcs.apply(sym);
                        SGGBuilderLLCanon.getInstance().dcsOrdered2canon.put(symDcs, canon);
                    }
                }
            }
            return canon;
        }
    }

    public OrderedDecisionSet getCanonical(OrderedDecisionSet dcs) {
        OrderedDecisionSet smallest = dcs;
        SymmetryIterator symIt = syms.iterator();
        // jump over identity
        symIt.next();
        for (SymmetryIterator iterator = symIt; iterator.hasNext();) {
            Symmetry sym = iterator.next();
            OrderedDecisionSet symDcs = dcs.apply(sym);
            if (symDcs.getIDChain().compareTo(smallest.getIDChain()) < 0) {
                smallest = symDcs;
            }
        }
        return smallest;
    }

    /**
     * TODO: Make it just a notification and the expensive transformation
     *
     * @param decisions
     * @param mcut
     * @param bad
     * @param game
     * @return
     */
    @Override
    public DecisionSet createDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {
        throw new UnsupportedOperationException("don't use this method");
//        return super.createDecisionSet(decisions, mcut, bad, game); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * TODO: Make it just a notification and the expensive transformation
     *
     * @param decision
     * @return
     */
    @Override
    public SysDecision createSysDecision(SysDecision decision) {
        throw new UnsupportedOperationException("don't use this method");
    }

    /**
     * TODO: Make it just a notification and the expensive transformation
     *
     * @param game
     * @param place
     * @param c
     * @return
     */
    @Override
    public SysDecision createSysDecision(PetriGameWithTransits game, Place place, CommitmentSet c) {
        throw new UnsupportedOperationException("don't use this method (only use the ordered one)");
    }

    public SysDecision createSysDecision(PetriGameWithTransits game, Place place, OrderedCommitmentSet c) {
        return super.createSysDecision(game, place, c);
    }

    /**
     * TODO: Make it just a notification and the expensive transformation
     *
     * @param game
     * @param transitions
     * @return
     */
    @Override
    public CommitmentSet createCommitmentSet(PetriGameWithTransits game, Set<Transition> transitions) {
        throw new UnsupportedOperationException("don't use this method (only use the ordered one (TreeSet))");
    }

    public OrderedCommitmentSet createCommitmentSet(PetriGameWithTransits game, TreeSet<Transition> transitions) {
        return new OrderedCommitmentSet(game, transitions);
    }

    @Override
    public OrderedCommitmentSet createCommitmentSet(PetriGameWithTransits game, boolean isTop) {
        return new OrderedCommitmentSet(game, isTop);
    }

    public OrderedCommitmentSet createCommitmentSet(OrderedCommitmentSet c) {
        return new OrderedCommitmentSet(c);
    }

    /**
     * TODO: Make it just a notification and the expensive transformation
     *
     * @param c
     * @return
     */
    @Override
    public CommitmentSet createCommitmentSet(CommitmentSet c) {
        throw new UnsupportedOperationException("don't use this method (only use the ordered one (TreeSet))");
    }

    @Override
    public Set<DecisionSet> resolveTop() {
        TreeSet<ILLDecision> dcs = new TreeSet<>(new LexiILLDecisionComparator());
        List<List<TreeSet<Transition>>> commitments = new ArrayList<>();
        List<Place> places = new ArrayList<>();
        for (Iterator<ILLDecision> iterator = getDecisionsIterator(); iterator.hasNext();) {
            ILLDecision decision = iterator.next();
            if (decision.isTop()) {
                Place p = decision.getPlace();
                places.add(p);
                List<TreeSet<Transition>> converted = getCommitments(p);
                commitments.add(converted);
            } else {
                dcs.add(decision);
            }
        }
        return createDecisionSets(places, commitments, dcs);
    }

    /**
     * Returns the resulting decisionsets (possibly more than one iff this is
     * not an mcut all possible solutions for commitment sets are used) or null
     * iff t is not firable.
     *
     * @param t
     * @return
     */
    @Override
    public Set<DecisionSet> fire(Transition t) {
        if (isBad()) {
            return null;
        }

        // create the new basic decisionset (could be more when succ of sys state (introducing the commitments))
        TreeSet<ILLDecision> ret = new TreeSet<>(new LexiILLDecisionComparator());

        // add all not impacted decisions (and check if it's firable)
        Set<Place> pre = new HashSet<>();
        for (Iterator<ILLDecision> iterator = getDecisionsIterator(); iterator.hasNext();) {
            ILLDecision dc = iterator.next();
            // when it is not chosen or the decision set place is not involved in the firing
            if (!dc.isChoosen(t) || !t.getPreset().contains(dc.getPlace())) {
                // then the complete decision is not involved in the firing and thus, stays
                ret.add(dc);
            } else { // otherwise the transition is chosen and the place in the preset, thus I could be used for firing
                pre.add(dc.getPlace());
            }
        }

        if (!pre.containsAll(t.getPreset())) {
            return null;
        }

        // Add the place for the postset
        // if it is an mcut this is just one successor where 
        // the commitments are set to top
        // Otherwise these are many successors (for all combinations of the 
        // powersets for all the commitments)
        List<List<TreeSet<Transition>>> commitments = new ArrayList<>();
        List<Place> places = new ArrayList<>();
        boolean hasTop = false;
        for (Place place : t.getPostset()) {
            if (getGame().isEnvironment(place)) {
                ret.add(createEnvDecision(getGame(), place));
            } else {
                if (isMcut()) {
                    ret.add(createSysDecision(getGame(), place, createCommitmentSet(getGame(), true)));
                    hasTop = true;
                } else {
                    places.add(place);
                    commitments.add(getCommitments(place));
                }
            }
        }
        if (isMcut()) {
            Set<DecisionSet> decisionsets = new HashSet<>();
            decisionsets.add(createDecisionSet(ret, !(hasTop || !checkMcut(ret)), calcBad(ret), getGame(), syms));
            return decisionsets;
        } else {
            if (places.isEmpty()) {
                Set<DecisionSet> decisionsets = new HashSet<>();
                decisionsets.add(createDecisionSet(ret, checkMcut(ret), calcBad(ret), getGame(), syms));
                return decisionsets;
            }
            return createDecisionSets(places, commitments, ret);
        }
    }

    /**
     * The same semantics as in DecisionSet, just transform it to an ordered
     * set.
     *
     * @param p
     * @return
     */
    private List<TreeSet<Transition>> getCommitments(Place p) {
        Set<Set<Transition>> powerset = Tools.powerSet(p.getPostset());
        List<TreeSet<Transition>> converted = new ArrayList<>();
        for (Set<Transition> set : powerset) {
            // Reduction technique of the MA of Valentin Spreckels:
            // When there is a transition with only one place in the preset
            // this transition is only allowed to appear solely in the commitment sets
            Collection<Transition> single = new ArrayList<>(set);
            single.retainAll((Collection<Transition>) getGame().getExtension("singlePresetTransitions"));
            if (single.isEmpty() || set.size() == 1) {
                // todo: check this could be expensive and maybe smarter to adapt the powerset construction
                TreeSet<Transition> c = new TreeSet(new LexiTransitionIDComparator());
                c.addAll(set);
                converted.add(c);
            }
        }
        return converted;
    }

    private Set<DecisionSet> createDecisionSets(List<Place> places, List<List<TreeSet<Transition>>> commitments, TreeSet<ILLDecision> dcs) {
        Set<DecisionSet> decisionsets = new HashSet<>();
        CartesianProduct<TreeSet<Transition>> prod = new CartesianProduct<>(commitments);
        for (Iterator<List<TreeSet<Transition>>> iterator = prod.iterator(); iterator.hasNext();) {
            List<TreeSet<Transition>> commitmentset = iterator.next();
            TreeSet<ILLDecision> newdcs = new TreeSet<>(dcs);
            for (int i = 0; i < commitmentset.size(); i++) {
                newdcs.add(createSysDecision(getGame(), places.get(i), createCommitmentSet(getGame(), commitmentset.get(i)))); // can only be SysDecisions because env should not use commitments
            }
            decisionsets.add(createDecisionSet(newdcs, checkMcut(newdcs), calcBad(newdcs), getGame(), syms));
        }
        return decisionsets;
    }

    /**
     * This method is only for the creation of the explicit graph
     * strategy.Attention: this returns not a copy but the real object!
     *
     *
     * @param game
     * @return
     */
    @Override
    @Deprecated
    public DecisionSet createLLDecisionSet(PetriGameWithTransits game) {
        return this;
    }

    @Override
    public OrderedDecisionSet apply(Symmetry sym) {
        TreeSet<ILLDecision> decs = new TreeSet<>(new LexiILLDecisionComparator());
        for (Iterator<ILLDecision> iterator = getDecisionsIterator(); iterator.hasNext();) {
            ILLDecision dc = iterator.next();
            decs.add((ILLDecision) dc.apply(sym));
        }
        return new OrderedDecisionSet(decs, isMcut(), isBad(), getGame(), syms);
    }

}
