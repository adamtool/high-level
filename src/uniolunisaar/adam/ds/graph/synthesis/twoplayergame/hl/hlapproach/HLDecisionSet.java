package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import uniol.apt.adt.extension.Extensible;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.ValuationIterator;
import uniolunisaar.adam.ds.synthesis.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.tools.CartesianProduct;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;

/**
 *
 * @author Manuel Gieseking
 */
public class HLDecisionSet extends Extensible implements IDecisionSet<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet> {

    private final Set<IHLDecision> decisions;
    private final boolean mcut;
    private final OneEnvHLPG hlgame;
    private final boolean bad;
//
//    public DecisionSet(Set<IDecision> decisions) {
//        this.decisions = decisions;
//        this.mcut = false;
//    }

    public HLDecisionSet(HLDecisionSet dcs) {
        this.mcut = dcs.mcut;
        this.hlgame = dcs.hlgame;
        this.decisions = new HashSet<>();
        for (IHLDecision decision : dcs.decisions) {
            if (decision.isEnvDecision()) {
                this.decisions.add(new HLEnvDecision((HLEnvDecision) decision));
            } else {
                this.decisions.add(new HLSysDecision((HLSysDecision) decision));
            }
        }
        this.bad = dcs.bad;
    }

    /**
     * ATTENTION: don't change the elements of the set afterwards, otherwise
     * contains won't work anymore
     *
     * @param decisions
     * @param mcut
     * @param bad
     * @param hlgame
     */
    public HLDecisionSet(Set<IHLDecision> decisions, boolean mcut, boolean bad, OneEnvHLPG hlgame) {
        this.decisions = decisions;
        this.mcut = mcut;
        this.hlgame = hlgame;
        this.bad = bad;
    }

    @Override
    public boolean hasTop(Set<IHLDecision> dcs) {
        for (IHLDecision decision : dcs) {
            if (decision.isTop()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasTop() {
        return hasTop(decisions);
    }

    @Override
    public Set<HLDecisionSet> resolveTop() {
        Set<IHLDecision> dcs = new HashSet<>(decisions);
        List<List<Set<ColoredTransition>>> commitments = new ArrayList<>();
        List<ColoredPlace> places = new ArrayList<>();
        for (IHLDecision decision : decisions) {
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
    private boolean firable(Set<IHLDecision> dcs, ColoredTransition t) {
        if (!t.isValid()) {
            return false;
        }
        for (ColoredPlace coloredPlace : t.getPreset()) { // no problem to check the same decision more than once, because of safe net
            boolean found = false;
            for (IHLDecision decision : dcs) {
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
     * Returns the resulting decision sets (possibly more than one iff this is
     * not an mcut all possible solutions for commitment sets are used) or null
     * iff t is not firable.
     *
     * @param t
     * @return
     */
    @Override
    public Set<HLDecisionSet> fire(ColoredTransition t) {
        if (bad) {
            return null;
        }
        if (!t.isValid()) { // only if the predicate is fullfilled
            return null;
        }
        // for each colored place in the preset of t look if the place
        // is contained in any decision set where t is chosen
        // iff there is one place for which we cannot find any decision set
        // the transition is not firable (we return null), otherwise collect
        // the decision set which are taken by firing this transition
        Set<IHLDecision> dcs = new HashSet<>();
        for (ColoredPlace coloredPlace : t.getPreset()) { // no problem to check the same decision more than once, because of safe net
            boolean found = false;
            for (IHLDecision decision : decisions) {
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
        // Take the old dcs an remove those which are taken by this transition
        Set<IHLDecision> ret = new HashSet<>(decisions);
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
                ret.add(new HLEnvDecision(coloredPlace));
            } else {
                if (mcut) {
                    ret.add(new HLSysDecision(coloredPlace, new HLCommitmentSet(true)));
                    hasTop = true;
                } else {
                    places.add(coloredPlace);
                    commitments.add(getCommitments(coloredPlace));
                }
            }
        }
        if (mcut) {
            Set<HLDecisionSet> decisionsets = new HashSet<>();
            decisionsets.add(new HLDecisionSet(ret, !(hasTop || !checkMcut(ret)), calcBad(ret), hlgame));
            return decisionsets;
        } else {
            if (places.isEmpty()) {
                Set<HLDecisionSet> decisionsets = new HashSet<>();
                decisionsets.add(new HLDecisionSet(ret, checkMcut(ret), calcBad(ret), hlgame));
                return decisionsets;
            }
            return createDecisionSets(places, commitments, ret);
        }
    }

    private List<Set<ColoredTransition>> getCommitments(ColoredPlace p) {
        Set<ColoredTransition> trans = new HashSet<>();
        for (Transition transition : p.getPlace().getPostset()) {
            for (ValuationIterator it = hlgame.getValuations(transition).iterator(); it.hasNext();) {
                Valuation val = it.next();
                ColoredTransition ct = new ColoredTransition(hlgame, transition, val);
                if (ct.isValid() && ct.getPreset().contains(p)) {
                    trans.add(ct);
                }
            }
        }
        Set<Set<ColoredTransition>> powerset = Tools.powerSet(trans);
        List<Set<ColoredTransition>> converted = new ArrayList<>();
        for (Set<ColoredTransition> set : powerset) {
            // Reduction technique of the MA of Valentin Spreckels:
            // When there is a transition with only one place in the preset
            // this transition is only allowed to appear solely in the commitment sets
            Collection<ColoredTransition> single = new ArrayList<>(set);
            single.retainAll(hlgame.getSinglePresetTransitions());
            if (single.isEmpty() || set.size() == 1) {
                converted.add(set);
            }
        }
        return converted;
    }

    private Set<HLDecisionSet> createDecisionSets(List<ColoredPlace> places, List<List<Set<ColoredTransition>>> commitments, Set<IHLDecision> dcs) {
        Set<HLDecisionSet> decisionsets = new HashSet<>();
        CartesianProduct<Set<ColoredTransition>> prod = new CartesianProduct<>(commitments);
        for (Iterator<List<Set<ColoredTransition>>> iterator = prod.iterator(); iterator.hasNext();) {
            List<Set<ColoredTransition>> commitmentset = iterator.next();
            Set<IHLDecision> newdcs = new HashSet<>(dcs);
            for (int i = 0; i < commitmentset.size(); i++) {
                newdcs.add(new HLSysDecision(places.get(i), new HLCommitmentSet(commitmentset.get(i)))); // can only be SysDecisions because env should not use commitments
            }
            decisionsets.add(new HLDecisionSet(newdcs, checkMcut(newdcs), calcBad(newdcs), hlgame));
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
    private boolean checkMcut(Set<IHLDecision> dcs) {
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

    @Override
    public Set<ColoredPlace> getMarking() {
        return getMarking(decisions);
    }

    /**
     *
     * This method is only for the creation of the explicit graph strategy.
     *
     * @param game
     * @return
     */
    @Override
    @Deprecated
    public DecisionSet createLLDecisionSet(PetriGameWithTransits game) {
        Set<ILLDecision> lldecisions = new HashSet<>();
        for (IHLDecision decision : this.decisions) {
            lldecisions.add(decision.toLLDecision(game));
        }
        return new DecisionSet(lldecisions, mcut, bad, null);
    }

    public Set<ColoredPlace> getMarking(Set<IHLDecision> dcs) {
        Set<ColoredPlace> marking = new HashSet<>();
        for (IHLDecision dc : dcs) {
            marking.add(dc.getPlace());
        }
        return marking;
    }

    private boolean calcNdet(Set<IHLDecision> dcs) {
        Set<ColoredPlace> marking = getMarking(dcs);
        Set<Transition> trans = hlgame.getTransitions();
        Set<ColoredTransition> choosenTrans = new HashSet<>();
        for (Transition t1 : trans) { // create low-level transition
            for (ValuationIterator it = hlgame.getValuations(t1).iterator(); it.hasNext();) {
                Valuation val = it.next();
                ColoredTransition ct = new ColoredTransition(hlgame, t1, val);
                if (!ct.isValid()) {
                    continue;
                }
                // check if it is choosen in dcs
                Set<ColoredPlace> preT1 = ct.getPreset();
                boolean choosen = true;
                for (IHLDecision decision : dcs) {
                    if (preT1.contains(decision.getPlace()) && !decision.isChoosen(ct)) {
                        choosen = false;
                    }
                }
                if (choosen) {
                    choosenTrans.add(ct);
                }
            }
        }
        for (ColoredTransition t1 : choosenTrans) { // todo: to stupid loops also tests t1 t2 and t2, t1.
            for (ColoredTransition t2 : choosenTrans) {
                if (!t1.equals(t2)) {
                    // sharing a system place?
                    Set<ColoredPlace> preT1 = t1.getPreset();
                    Set<ColoredPlace> preT2 = t2.getPreset();
                    Set<ColoredPlace> intersect = new HashSet<>(preT1);
                    intersect.retainAll(preT2);
                    boolean shared = false;
                    for (ColoredPlace place : intersect) {
                        if (!hlgame.isEnvironment(place.getPlace()) && marking.contains(place)) {
                            shared = true;
                        }
                    }
                    if (shared && hlgame.eventuallyEnabled(t1, t2)) { // here check added for firing in the original game
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean calcDeadlock(Set<IHLDecision> dcs) {
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
                    for (IHLDecision decision : dcs) {
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

    private boolean calcBadPlace(Set<IHLDecision> dcs) {
        for (IHLDecision dc : dcs) {
            if (hlgame.isBad(dc.getPlace().getPlace())) {
                return true;
            }
        }
        return false;
    }

    private boolean calcBad(Set<IHLDecision> dcs) {
        return calcBadPlace(dcs) || calcDeadlock(dcs) || calcNdet(dcs);
//        return calcDeadlock(dcs);// || calcNdet(dcs);
//        return calcBadPlace(dcs);// || calcDeadlock(dcs) || calcNdet(dcs);
//return false;
    }

//    public void apply(Symmetry sym) {
//        for (IHLDecision decision : decisions) {
//            decision.apply(sym);
//        }
//    }
    @Override
    public HLDecisionSet apply(Symmetry sym) {
        Set<IHLDecision> decs = new HashSet<>();
        for (IHLDecision decision : decisions) {
            decs.add((IHLDecision) decision.apply(sym));
        }
        return new HLDecisionSet(decs, mcut, bad, hlgame);
    }

    @Override
    public boolean isMcut() {
        return mcut;
    }

    @Override
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
        hash = 31 * hash * Objects.hashCode(this.decisions);
        hash = hash * (this.mcut ? 2 : 1);
        hash = hash * (this.bad ? 3 : 1);
//        hash = 31 * hash + (this.mcut ? 1 : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HLDecisionSet other = (HLDecisionSet) obj;
        if (this.mcut != other.mcut) {
            return false;
        }
        if (this.bad != other.bad) {
            return false;
        }
        if (!Objects.equals(this.decisions, other.decisions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        for (IHLDecision dc : decisions) {
            sb.append(dc.toDot()).append("\\n");
        }
        if (decisions.size() >= 1) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (IHLDecision dc : decisions) {
            sb.append(dc.toString()).append("\n");
        }
        if (decisions.size() >= 1) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }
}
