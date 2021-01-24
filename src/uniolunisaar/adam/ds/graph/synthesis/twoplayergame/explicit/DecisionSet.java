package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import uniol.apt.adt.exception.StructureException;
import uniol.apt.adt.extension.Extensible;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.tools.CartesianProduct;
import uniolunisaar.adam.tools.Tools;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecisionSet;

/**
 *
 * @author Manuel Gieseking
 */
public class DecisionSet extends Extensible implements IDecisionSet<Place, Transition, ILLDecision, DecisionSet> {

    private final Set<ILLDecision> decisions;
    private final boolean mcut;
    private final PetriGameWithTransits game;
    private final boolean bad;
    private int id = -1;

    /**
     * It is not that nice to set IDs later, but for using it in the graphs and
     * allow it the create independent from it, it was easier this way.
     *
     * @param id
     */
    @Override
    public void setId(int id) {
        if (this.id != -1) {
            throw new StructureException("Cannot set id to " + id + " because this decision set already has the id " + this.id);
        }
        this.id = id;
    }

    @Override
    public void overwriteId(int id) {
        this.id = id;
    }

//
//    public DecisionSet(Set<IDecision> decisions) {
//        this.decisions = decisions;
//        this.mcut = false;
//    }
    public DecisionSet(DecisionSet dcs) {
        this.mcut = dcs.mcut;
        this.game = dcs.game;
        this.decisions = new HashSet<>();
        for (ILLDecision decision : dcs.decisions) {
            if (decision.isEnvDecision()) {
                this.decisions.add(createEnvDecision((EnvDecision) decision));
            } else {
                this.decisions.add(createSysDecision((SysDecision) decision));
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
    public DecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {
        this.decisions = decisions;
        this.mcut = mcut;
        this.game = game;
        this.bad = bad;
    }

    public EnvDecision createEnvDecision(EnvDecision decision) {
        return new EnvDecision(decision);
    }

    public EnvDecision createEnvDecision(PetriGameWithTransits game, Place place) {
        return new EnvDecision(game, place);
    }

    public SysDecision createSysDecision(SysDecision decision) {
        return new SysDecision(decision);
    }

    public SysDecision createSysDecision(PetriGameWithTransits game, Place place, CommitmentSet c) {
        return new SysDecision(game, place, c);
    }

    public CommitmentSet createCommitmentSet(CommitmentSet c) {
        return new CommitmentSet(c);
    }

    public CommitmentSet createCommitmentSet(PetriGameWithTransits game, boolean isTop) {
        return new CommitmentSet(game, isTop);
    }

    public CommitmentSet createCommitmentSet(PetriGameWithTransits game, Set<Transition> transitions) {
        return new CommitmentSet(game, transitions);
    }

    public DecisionSet createDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {
        return new DecisionSet(decisions, mcut, bad, game);
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
    public Set<DecisionSet> resolveTop() {
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
    public Set<DecisionSet> fire(Transition t) {
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
                ret.add(createEnvDecision(game, place));
            } else {
                if (mcut) {
                    ret.add(createSysDecision(game, place, createCommitmentSet(game, true)));
                    hasTop = true;
                } else {
                    places.add(place);
                    commitments.add(getCommitments(place));
                }
            }
        }
        if (mcut) {
            Set<DecisionSet> decisionsets = new HashSet<>();
            decisionsets.add(createDecisionSet(ret, !(hasTop || !checkMcut(ret)), calcBad(ret), game));
            return decisionsets;
        } else {
            if (places.isEmpty()) {
                Set<DecisionSet> decisionsets = new HashSet<>();
                decisionsets.add(createDecisionSet(ret, checkMcut(ret), calcBad(ret), game));
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
            Collection<Transition> single = new ArrayList<>(set);
            single.retainAll((Collection<Transition>) game.getExtension("singlePresetTransitions"));
            if (single.isEmpty() || set.size() == 1) {
                converted.add(set);
            }
        }
        return converted;
    }

    private Set<DecisionSet> createDecisionSets(List<Place> places, List<List<Set<Transition>>> commitments, Set<ILLDecision> dcs) {
        Set<DecisionSet> decisionsets = new HashSet<>();
        CartesianProduct<Set<Transition>> prod = new CartesianProduct<>(commitments);
        for (Iterator<List<Set<Transition>>> iterator = prod.iterator(); iterator.hasNext();) {
            List<Set<Transition>> commitmentset = iterator.next();
            Set<ILLDecision> newdcs = new HashSet<>(dcs);
            for (int i = 0; i < commitmentset.size(); i++) {
                newdcs.add(createSysDecision(game, places.get(i), createCommitmentSet(game, commitmentset.get(i)))); // can only be SysDecisions because env should not use commitments
            }
            decisionsets.add(createDecisionSet(newdcs, checkMcut(newdcs), calcBad(newdcs), game));
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
    protected boolean checkMcut(Set<ILLDecision> dcs) {
        for (Transition t : (Collection<Transition>) game.getExtension("sysTransitions")) { // todo: quick hack
            if (firable(dcs, t)) {
                return false;
            }
        }
        return true;
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
    public Set<Place> getMarking() {
        return getMarking(decisions);
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

    protected boolean calcBad(Set<ILLDecision> dcs) {
        return calcBadPlace(dcs) || calcDeadlock(dcs) || calcNdet(dcs);
//        return calcDeadlock(dcs);// || calcNdet(dcs);
//        return calcBadPlace(dcs) ;//|| calcDeadlock(dcs) || calcNdet(dcs);
//        return calcNdet(dcs);
//        return false;
    }

//    @Override
//    public void apply(Symmetry sym) {
//        for (ILLDecision decision : decisions) {
//            decision.apply(sym);
//        }
//    }
    @Override
    public DecisionSet apply(Symmetry sym) {
        Set<ILLDecision> decs = new HashSet<>();
        for (ILLDecision decision : decisions) {
            decs.add((ILLDecision) decision.apply(sym));
        }
        return createDecisionSet(decs, mcut, bad, game);
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
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 41;
        hash = 37 * hash * Objects.hashCode(this.decisions);
        hash = hash * (this.mcut ? 2 : 1);
        hash = hash * (this.bad ? 3 : 1);
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
        final DecisionSet other = (DecisionSet) obj;
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
        StringBuilder sbEnv = new StringBuilder();
        StringBuilder sbSys = new StringBuilder();
        for (ILLDecision dc : decisions) {
            if (dc.isEnvDecision()) {
                sbEnv.append(dc.toDot()).append("\\n");
            } else {
                sbSys.append(dc.toDot()).append("\\n");
            }
        }
        if (decisions.size() >= 1) {
            sbSys.delete(sbSys.length() - 2, sbSys.length());
        }
        return sbEnv.toString() + sbSys.toString();
    }

    @Override
    public String toString() {
        return toDot();
    }

    public PetriGameWithTransits getGame() {
        return game;
    }

    public Iterator<ILLDecision> getDecisionsIterator() {
        return decisions.iterator();
    }

    /**
     * Returns a concatenated String of all IDChains of the decisions. If the
     * set is ordered it obeys the order.
     *
     * @return
     */
    public String getIDChain() {
        if (decisions == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<ILLDecision> iterator = decisions.iterator(); iterator.hasNext();) {
            sb.append(iterator.next().getIDChain()).append("|");
        }
        return sb.toString();
    }
}
