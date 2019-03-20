package uniolunisaar.adam.ds.graph.hl.approachLL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import uniol.apt.adt.extension.Extensible;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.DecisionSet;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.tools.CartesianProduct;
import uniolunisaar.adam.tools.Tools;

/**
 *
 * @author Manuel Gieseking
 */
public class LLDecisionSet extends Extensible implements DecisionSet<Place, Transition, ILLDecision> {

    private final Set<ILLDecision> decisions;
    private final boolean mcut;
    private final PetriGame game;
    private final boolean bad;
//
//    public DecisionSet(Set<IDecision> decisions) {
//        this.decisions = decisions;
//        this.mcut = false;
//    }

    public LLDecisionSet(LLDecisionSet dcs) {
        this.mcut = dcs.mcut;
        this.game = dcs.game;
        this.decisions = new HashSet<>();
        for (ILLDecision decision : dcs.decisions) {
            if (decision.isEnvDecision()) {
                this.decisions.add(new LLEnvDecision((LLEnvDecision) decision));
            } else {
                this.decisions.add(new LLSysDecision((LLSysDecision) decision));
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
     * @param game
     */
    public LLDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGame game) {
        this.decisions = decisions;
        this.mcut = mcut;
        this.game = game;
        this.bad = bad;
    }

    @Override
    public boolean hasTop(Set<ILLDecision> dcs) {
        for (ILLDecision decision : dcs) {
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
    public Set<LLDecisionSet> resolveTop() {
        Set<ILLDecision> dcs = new HashSet<>(decisions);
        List<List<Set<Transition>>> commitments = new ArrayList<>();
        List<Place> places = new ArrayList<>();
        for (ILLDecision decision : decisions) {
            if (decision.isTop()) {
                dcs.remove(decision);
                Place p = decision.getPlace();
                places.add(p);
                List<Set<Transition>> converted = getCommitments(p);
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
    private boolean firable(Set<ILLDecision> dcs, Transition t) {
        for (Place place : t.getPreset()) { // no problem to check the same decision more than once, because of safe net
            boolean found = false;
            for (ILLDecision decision : dcs) {
                if (decision.isChoosen(t) && decision.getPlace().equals(place)) {
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
    @Override
    public Set<LLDecisionSet> fire(Transition t) {
        if (bad) {
            return null;
        }
        // for each place in the preset of t check if the place
        // is contained in any decision set where t is chosen
        // iff there is one place for which we cannot find any decision set
        // the transition is not firable (we return null), otherwise collect
        // the decision set which are taken by firing this transition
        Set<ILLDecision> dcs = new HashSet<>();
        for (Place Place : t.getPreset()) { // no problem to check the same decision more than once, because of safe net
            boolean found = false;
            for (ILLDecision decision : decisions) {
                if (decision.isChoosen(t) && decision.getPlace().equals(Place)) {
                    dcs.add(decision);
                    found = true;
                    break; // one is enough
                }
            }
            if (!found) { // there was no decision set with this place where t was chosen
                return null; // not firable
            }
        }
        // Take the old dcs an remove those which are taken by this transition
        Set<ILLDecision> ret = new HashSet<>(decisions);
        ret.removeAll(dcs);

        // Add the place for the postset
        // if it is an mcut this is just one successor where 
        // the commitments are set to top
        // Otherwise these are many successors (for all combinations of the 
        // powersets for all the commitments)
        List<List<Set<Transition>>> commitments = new ArrayList<>();
        List<Place> places = new ArrayList<>();
        boolean hasTop = false;
        for (Place place : t.getPostset()) {
            if (game.isEnvironment(place)) {
                ret.add(new LLEnvDecision(game, place));
            } else {
                if (mcut) {
                    ret.add(new LLSysDecision(game, place, new LLCommitmentSet(game, true)));
                    hasTop = true;
                } else {
                    places.add(place);
                    commitments.add(getCommitments(place));
                }
            }
        }
        if (mcut) {
            Set<LLDecisionSet> decisionsets = new HashSet<>();
            decisionsets.add(new LLDecisionSet(ret, !(hasTop || !checkMcut(ret)), calcBad(ret), game));
            return decisionsets;
        } else {
            if (places.isEmpty()) {
                Set<LLDecisionSet> decisionsets = new HashSet<>();
                decisionsets.add(new LLDecisionSet(ret, checkMcut(ret), calcBad(ret), game));
                return decisionsets;
            }
            return createDecisionSets(places, commitments, ret);
        }
    }

    private List<Set<Transition>> getCommitments(Place p) {
        Set<Set<Transition>> powerset = Tools.powerSet(p.getPostset());
        List<Set<Transition>> converted = new ArrayList<>();
        for (Set<Transition> set : powerset) {
            // Reduction technique of the MA of Valentin Spreckels:
            // When there is a transition with only one place in the preset
            // this transition is only allowed to appear solely in the commitment sets
            Collection<Transition> single = new ArrayList(set);
            single.retainAll((Collection<Transition>) game.getExtension("singlePresetTransitions"));
            if (single.isEmpty() || set.size() == 1) {
                converted.add(set);
            }
        }
        return converted;
    }

    private Set<LLDecisionSet> createDecisionSets(List<Place> places, List<List<Set<Transition>>> commitments, Set<ILLDecision> dcs) {
        Set<LLDecisionSet> decisionsets = new HashSet<>();
        CartesianProduct<Set<Transition>> prod = new CartesianProduct<>(commitments);
        for (Iterator<List<Set<Transition>>> iterator = prod.iterator(); iterator.hasNext();) {
            List<Set<Transition>> commitmentset = iterator.next();
            Set<ILLDecision> newdcs = new HashSet<>(dcs);
            for (int i = 0; i < commitmentset.size(); i++) {
                newdcs.add(new LLSysDecision(game, places.get(i), new LLCommitmentSet(game, commitmentset.get(i)))); // can only be SysDecisions because env should not use commitments
            }
            decisionsets.add(new LLDecisionSet(newdcs, checkMcut(newdcs), calcBad(newdcs), game));
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
    private boolean checkMcut(Set<ILLDecision> dcs) {
        for (Transition t : (Collection<Transition>) game.getExtension("sysTransitions")) { // todo: quick hack
            if (firable(dcs, t)) {
                return false;
            }
        }
        return true;
    }

    private Set<Place> getMarking(Set<ILLDecision> dcs) {
        Set<Place> marking = new HashSet<>();
        for (ILLDecision dc : dcs) {
            marking.add(dc.getPlace());
        }
        return marking;
    }

    private boolean calcNdet(Set<ILLDecision> dcs) {
        Set<Place> marking = getMarking(dcs);
        Set<Transition> trans = game.getTransitions();
        Set<Transition> choosenTrans = new HashSet<>();
        for (Transition t : trans) {
            // check if it is choosen in dcs
            Set<Place> preT1 = t.getPreset();
            boolean choosen = true;
            for (ILLDecision decision : dcs) {
                if (!(!preT1.contains(decision.getPlace()) || decision.isChoosen(t))) {
                    choosen = false;
                }
            }
            if (choosen) {
                choosenTrans.add(t);
            }
        }
        for (Transition t1 : choosenTrans) { // create low-level transition
            for (Transition t2 : choosenTrans) {
                if (!t1.getId().equals(t2.getId())) {
                    // sharing a system place?
                    Set<Place> preT1 = t1.getPreset();
                    Set<Place> preT2 = t2.getPreset();
                    Set<Place> intersect = new HashSet<>(preT1);
                    intersect.retainAll(preT2);
                    boolean shared = false;
                    for (Place place : intersect) {
                        if (!game.isEnvironment(place) && marking.contains(place)) {
                            shared = true;
                        }
                    }
                    if (shared && game.eventuallyEnabled(t1, t2)) { // here check added for firing in the original game
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean calcDeadlock(Set<ILLDecision> dcs) {
        if (hasTop(dcs)) {
            return false;
        }
        boolean existsEnabled = false;
        for (Transition t : game.getTransitions()) {
            // the following code is nearly copied from firable, but we directly add
            // it here to save calculation time and doing the exists enabled and but nothing
            // firable directly together
            boolean isEnabled = true;
            boolean isFirable = true;
            for (Place place : t.getPreset()) { // no problem to check the same decision more than once, because of safe net
                boolean foundEnabled = false;
                boolean foundFirable = false;
                for (ILLDecision decision : dcs) {
                    if (decision.getPlace().equals(place)) {
                        foundEnabled = true;
                        if (decision.isChoosen(t)) {
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
        return existsEnabled;
    }

    private boolean calcBadPlace(Set<ILLDecision> dcs) {
        for (ILLDecision dc : dcs) {
            if (game.isBad(dc.getPlace())) {
                return true;
            }
        }
        return false;
    }

    private boolean calcBad(Set<ILLDecision> dcs) {
        return calcBadPlace(dcs) || calcDeadlock(dcs) || calcNdet(dcs);
//        return calcNdet(dcs);
    }

//    @Override
//    public void apply(Symmetry sym) {
//        for (ILLDecision decision : decisions) {
//            decision.apply(sym);
//        }
//    }
    @Override
    public LLDecisionSet apply(Symmetry sym) {
        Set<ILLDecision> decs = new HashSet<>();
        for (ILLDecision decision : decisions) {
            decs.add((ILLDecision) decision.apply(sym));
        }
        return new LLDecisionSet(decs, mcut, bad, game);
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
        hash = 31 * hash + Objects.hashCode(this.decisions);
//        hash = 31 * hash * (this.mcut ? 2 : 1);
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
        final LLDecisionSet other = (LLDecisionSet) obj;
        if (this.mcut != other.mcut) {
            return false;
        }
        if (this.bad != other.bad) {
            return false;
        }
//        for (ILLDecision decision : this.decisions) {
//            if (!other.decisions.contains(decision)) {
//                return false;
//            }
//        }
//        for (ILLDecision decision : other.decisions) {
//            if (!this.decisions.contains(decision)) {
//                if (SGGBuilder.depth < 10) {
//                    System.out.println(decision);
//                }
//                return false;
//            }
//        }
        if (!Objects.equals(this.decisions, other.decisions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        for (ILLDecision dc : decisions) {
            sb.append(dc.toDot()).append("\\n");
        }
        if (decisions.size() >= 1) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return toDot();
    }
}
