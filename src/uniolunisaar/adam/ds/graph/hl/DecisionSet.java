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
    private final boolean bad;
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
        this.bad = dcs.bad;
    }
    
    public DecisionSet(Set<IDecision> decisions, boolean mcut, boolean bad, OneEnvHLPG hlgame) {
        this.decisions = decisions;
        this.mcut = mcut;
        this.hlgame = hlgame;
        this.bad = bad;
    }
    
    public boolean hasTop(Set<IDecision> dcs) {
        for (IDecision decision : dcs) {
            if (decision.isTop()) {
                return true;
            }
        }
        return false;
    }

    public boolean hasTop() {
        return hasTop(decisions);
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
     * copied part from there and the other method saves time.
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
        if (!t.isValid()) { // only if the predicate is fullfilled
            return null;
        }
        // for each colored place in the preset of t look if the place
        // is contained in any decision set where t is chosen
        // iff there is one place for which we cannot find any decision set
        // the transition is not firable (we return null), otherwise collect
        // the decision set which are taken by firing this transition
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
            if (!found) { // there was no decision set with this colored place where t was chosen
                return null; // not firable
            }
        }
        System.out.println("did it");
        // Take the old dcs an remove those which are taken by this transition
        Set<IDecision> ret = new HashSet<>(decisions);
        ret.removeAll(dcs);

        // Add the colored place for the postset
        // if it is an mcut this is just one successor where 
        // the commitments are set to top
        // Otherwise these are many successors (for all combinations of the 
        // powersets for all the commitments)
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
            decisionsets.add(new DecisionSet(ret, !(hasTop || !checkMcut(ret)), calcBad(ret), hlgame));
            return decisionsets;
        } else {
            return createDecisionSets(places, commitments, ret);
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
            decisionsets.add(new DecisionSet(newdcs, checkMcut(newdcs), calcBad(newdcs), hlgame));
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
    
    private boolean calcNdet(Set<IDecision> dcs) {
        return false; //todo: finish
    }
    
    private boolean calcDeadlock(Set<IDecision> dcs) {
        if (hasTop(dcs)) {
            return false;
        }
        boolean existsEnabled = false;
        for (Transition t : hlgame.getTransitions()) {
            for (ValuationIterator it = hlgame.getValuations(t).iterator(); it.hasNext();) {
                Valuation val = it.next();
                ColoredTransition ct = new ColoredTransition(hlgame, t, val);
                // the following code is nearly copied from firable, but we directly add
                // it here to save calculation time and doing the exists enabled and but nothing
                // firable directly together
                if (!ct.isValid()) {
                    continue;
                }
                boolean isEnabled = true;
                boolean isFirable = true;
                for (ColoredPlace coloredPlace : ct.getPreset()) { // no problem to check the same decision more than once, because of safe net
                    boolean foundEnabled = false;
                    boolean foundFirable = false;
                    for (IDecision decision : dcs) {
                        if (decision.getPlace().equals(coloredPlace)) {
                            foundEnabled = true;
                            if (decision.isChoosen(ct)) {
                                foundFirable = true;
                                break; // one is enough
                            }
                        }
                    }
                    if (!foundEnabled) {
                        isEnabled = false;
                    }
                    if (!foundFirable) {
                        isFirable = false;
                    }
                }
                if (isEnabled) {
                    existsEnabled = true;
                }
                if (isFirable) {
                    return false;
                }
            }
        }
        return existsEnabled;
    }
    
    private boolean calcBadPlace(Set<IDecision> dcs) {
        for (IDecision dc : dcs) {
            if (hlgame.isBad(dc.getPlace().getPlace())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean calcBad(Set<IDecision> dcs) {
        return calcBadPlace(dcs) || calcDeadlock(dcs) || calcNdet(dcs);
    }
    
    public void apply(Symmetry sym) {
        for (IDecision decision : decisions) {
            decision.apply(sym);
        }
    }
    
    public boolean isMcut() {
        return mcut;
    }
    
    public boolean isBad() {
        return bad;
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
    
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        for (IDecision dc : decisions) {
            sb.append(dc.toDot()).append("\\n");
        }
        if (decisions.size() >= 1) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }
    
}
