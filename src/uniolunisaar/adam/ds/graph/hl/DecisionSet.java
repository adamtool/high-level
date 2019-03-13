package uniolunisaar.adam.ds.graph.hl;

import java.util.HashSet;
import java.util.Set;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public class DecisionSet {

    private final Set<IDecision> decisions;
    private final boolean mcut;
//    private final HLPetriGame hlgame;
//
//    public DecisionSet(Set<IDecision> decisions) {
//        this.decisions = decisions;
//        this.mcut = false;
//    }

//    public DecisionSet(Set<IDecision> decisions, boolean mcut, HLPetriGame hlgame) {
    public DecisionSet(Set<IDecision> decisions, boolean mcut) {
        this.decisions = decisions;
        this.mcut = mcut;
//        this.hlgame = hlgame;
    }
// TODO: how to nicely obtain all the possibilities
//    public Set<DecisionSet> resolveTop() {
//        Set<IDecision> dcs = new HashSet<>(decisions);
//        for (IDecision decision : decisions) {
//            if (decision.isTop()) {
//                dcs.remove(decision);
//                ColoredPlace p = decision.getPlace();               
//                    dcs.add(new SysDecision(p, new CommitmentSet(true))); // env places cannot have a top
//               
//            }
//        }
//    }

    /**
     * Returns the resulting decisionset or null iff t is not firable.
     *
     * @param t
     * @return
     */
    public DecisionSet fire(ColoredTransition t) {
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
        boolean hasTop = false;
        for (ColoredPlace coloredPlace : t.getPostset()) {
            if (t.getHlgame().isEnvironment(coloredPlace.getPlace())) {
                ret.add(new EnvDecision(coloredPlace));
            } else {
                ret.add(new SysDecision(coloredPlace, new CommitmentSet(true)));
                hasTop = true;
            }
        }
        return new DecisionSet(dcs, !hasTop || checkMcut(dcs));
    }

    private boolean checkMcut(Set<IDecision> dcs) {
        return false; // todo: do it
    }
}
