package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.CommitmentSet;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class LLCommitmentSet extends CommitmentSet {

    public LLCommitmentSet(PetriGameWithTransits game, boolean isTop) {
        super(game, isTop);
    }

    public LLCommitmentSet(PetriGameWithTransits game, Transition... transitions) {
        super(game, transitions);
    }

    public LLCommitmentSet(PetriGameWithTransits game, Set<Transition> transitions) {
        super(game, transitions);
    }

    public LLCommitmentSet(LLCommitmentSet c) {
        super(c);
    }

    public LLCommitmentSet(CommitmentSet c) {
        super(c);
    }

//    @Override
//    public void apply(Symmetry sym) {
//        if (isTop()) {
//            return;
//        }
//        Set<Transition> tr = new HashSet<>();
//        for (Transition transition : transitions) {
//            String hlID = HL2PGConverter.getOrigID(transition);
//            Valuation val = HL2PGConverter.getValuation(transition);
//            Valuation newVal = new Valuation();
//            for (Map.Entry<Variable, Color> entry : val.entrySet()) {
//                Variable var = entry.getKey();
//                Color c = entry.getValue();
//                newVal.put(var, sym.get(c));
//            }
//            tr.add(game.getTransition(HL2PGConverter.getTransitionID(hlID, newVal)));
//        }
//        transitions = tr;
//    }
    @Override
    public LLCommitmentSet apply(Symmetry sym) {
        if (isTop()) {
            return new LLCommitmentSet(super.getGame(), true);
        }
        Set<Transition> tr = new HashSet<>();
        for (Transition transition : getTransitions()) {
            String hlID = HL2PGConverter.getOrigID(transition);
            Valuation val = HL2PGConverter.getValuation(transition);
            Valuation newVal = new Valuation();
            for (Map.Entry<Variable, Color> entry : val.entrySet()) {
                Variable var = entry.getKey();
                Color c = entry.getValue();
                newVal.put(var, sym.get(c));
            }
            tr.add(getGame().getTransition(HL2PGConverter.getTransitionID(hlID, newVal)));
        }
        return new LLCommitmentSet(getGame(), tr);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (isTop() ? 1 : 0);
        int tr = 1;
        if (getTransitions() != null) {
            for (Transition transition : getTransitions()) {
                tr *= HL2PGConverter.getHashCode(transition);
            }
        }
        hash = 13 * hash * tr;
        return hash;
    }
}
