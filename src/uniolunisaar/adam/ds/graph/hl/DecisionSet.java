package uniolunisaar.adam.ds.graph.hl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.ValuationIterator;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.tools.CartesianProduct;
import uniolunisaar.adam.tools.Tools;

/**
 *
 * @author Manuel Gieseking
 */
public class DecisionSet extends SRGState {

    private final Set<IDecision> decisions;
    private final boolean mcut;
    private final OneEnvHLPG hlgame;
//
//    public DecisionSet(Set<IDecision> decisions) {
//        this.decisions = decisions;
//        this.mcut = false;
//    }

    public DecisionSet(DecisionSet dcs) {
        this.mcut = dcs.mcut;
        this.hlgame = dcs.hlgame;
        this.decisions = new HashSet<>();
        for (IDecision decision : dcs.decisions) {
            if (decision.isEnvDecision()) {
                this.decisions.add(new EnvDecision((EnvDecision) decision));
            } else {
                this.decisions.add(new SysDecision((SysDecision) decision));
            }
        }
    }

    public DecisionSet(Set<IDecision> decisions, boolean mcut, OneEnvHLPG hlgame) {
        this.decisions = decisions;
        this.mcut = mcut;
        this.hlgame = hlgame;
    }

    public boolean hasTop() {
        for (IDecision decision : decisions) {
            if (decision.isTop()) {
                return true;
            }
        }
        return false;
    }

    public Set<DecisionSet> resolveTop() {
        Set<IDecision> dcs = new HashSet<>(decisions);
        List<List<Set<ColoredTransition>>> commitments = new ArrayList<>();
        List<ColoredPlace> places = new ArrayList<>();
        for (IDecision decision : decisions) {
            if (decision.isTop()) {
                dcs.remove(decision);
                ColoredPlace p = decision.getPlace();
                places.add(p);
                List<Set<ColoredTransition>> converted = getCommitments(p);
                commitments.add(converted);
            }
        }
        return createDecisionSets(places, commitments, dcs);
    }

    /**
     * Don't use this method when you want to afterwards fire the transition.
     * Therefore use fire and check if the result is null. This code is just the
     * copied part from there and the other methods saves time.
     *
     * @param t
     * @return
     */
    private boolean firable(Set<IDecision> dcs, ColoredTransition t) {
        if (!t.isValid()) {
            return false;
        }
        for (ColoredPlace coloredPlace : t.getPreset()) { // no problem to check the same decision more than once, because of safe net
            boolean found = false;
            for (IDecision decision : dcs) {
                if (decision.isChoosen(t) && decision.getPlace().equals(coloredPlace)) {
                    found = true;
                    break; // one is enough
                }
            }
            if (!found) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the resulting decisionsets (possibly more than one iff this is
     * not an mcut all possible solutions for commitment sets are used) or null
     * iff t is not firable.
     *
     * @param t
     * @return
     */
    public Set<DecisionSet> fire(ColoredTransition t) {
        if (!t.isValid()) {
            return null;
        }
        Set<IDecision> dcs = new HashSet<>();
        for (ColoredPlace coloredPlace : t.getPreset()) { // no problem to check the same decision more than once, because of safe net
            boolean found = false;
            for (IDecision decision : decisions) {
                if (decision.isChoosen(t) && decision.getPlace().equals(coloredPlace)) {
                    dcs.add(decision);
                    found = true;
                    break; // one is enough
                }
            }
            if (!found) {
                return null;
            }
        }
        Set<IDecision> ret = new HashSet<>(decisions);
        ret.removeAll(dcs);

        List<List<Set<ColoredTransition>>> commitments = new ArrayList<>();
        List<ColoredPlace> places = new ArrayList<>();
        boolean hasTop = false;
        for (ColoredPlace coloredPlace : t.getPostset()) {
            if (hlgame.isEnvironment(coloredPlace.getPlace())) {
                ret.add(new EnvDecision(coloredPlace));
            } else {
                if (mcut) {
                    ret.add(new SysDecision(coloredPlace, new CommitmentSet(true)));
                    hasTop = true;
                } else {
                    places.add(coloredPlace);
                    commitments.add(getCommitments(coloredPlace));
                }
            }
        }
        if (mcut) {
            Set<DecisionSet> decisionsets = new HashSet<>();
            decisionsets.add(new DecisionSet(dcs, !hasTop || checkMcut(dcs), hlgame));
            return decisionsets;
        } else {
            return createDecisionSets(places, commitments, dcs);
        }
    }

    private List<Set<ColoredTransition>> getCommitments(ColoredPlace p) {
        Set<ColoredTransition> trans = new HashSet<>();
        for (Transition transition : p.getPlace().getPostset()) {
            for (ValuationIterator it = hlgame.getValuations(transition).iterator(); it.hasNext();) {
                Valuation val = it.next();
                trans.add(new ColoredTransition(hlgame, transition, val));
            }
        }
        Set<Set<ColoredTransition>> powerset = Tools.powerSet(trans);
        List<Set<ColoredTransition>> converted = new ArrayList<>();
        for (Set<ColoredTransition> set : powerset) {
            converted.add(set);
        }
        return converted;
    }

    private Set<DecisionSet> createDecisionSets(List<ColoredPlace> places, List<List<Set<ColoredTransition>>> commitments, Set<IDecision> dcs) {
        Set<DecisionSet> decisionsets = new HashSet<>();
        CartesianProduct<Set<ColoredTransition>> prod = new CartesianProduct<>(commitments);
        for (Iterator<List<Set<ColoredTransition>>> iterator = prod.iterator(); iterator.hasNext();) {
            List<Set<ColoredTransition>> commitmentset = iterator.next();
            Set<IDecision> newdcs = new HashSet<>(dcs);
            for (int i = 0; i < commitmentset.size(); i++) {
                newdcs.add(new SysDecision(places.get(i), new CommitmentSet(commitmentset.get(i)))); // can only be SysDecisions because env should not use commitments
            }
            decisionsets.add(new DecisionSet(newdcs, checkMcut(newdcs), hlgame));
        }
        return decisionsets;
    }

    /**
     * Attention currently does not check if contains top, because any current
     * call knows this in advance (saves calculation time)
     *
     * @param dcs
     * @return
     */
    private boolean checkMcut(Set<IDecision> dcs) {
        for (Transition t : hlgame.getSystemTransitions()) {
            for (ValuationIterator it = hlgame.getValuations(t).iterator(); it.hasNext();) {
                Valuation val = it.next();
                if (firable(dcs, new ColoredTransition(hlgame, t, val))) {
                    return false;
                }
            }
        }
        return true;
    }

    public void apply(Symmetry sym) {
        for (IDecision decision : decisions) {
            decision.apply(sym);
        }
    }

    public boolean isMcut() {
        return mcut;
    }

    @Override
    public int getId() {
        return hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(this.decisions);
        hash = 31 * hash + (this.mcut ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj
    ) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DecisionSet other = (DecisionSet) obj;
        if (this.mcut != other.mcut) {
            return false;
        }
        if (!Objects.equals(this.decisions, other.decisions)) {
            return false;
        }
        return true;
    }

}
