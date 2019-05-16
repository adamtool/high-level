package uniolunisaar.adam.ds.graph.hl.approachLL;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.CommitmentSet;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class LLCommitmentSet extends CommitmentSet<Transition> {

    private final PetriGame game;

    public LLCommitmentSet(PetriGame game, boolean isTop) {
        super(isTop);
        this.game = game;
    }

    /**
     * Attention this uses just the references of the transitions of the given
     * list and the list itself. Don't change them afterward, they are saved as
     * HashSet and thus contains would not longer work!
     *
     * @param game
     * @param transitions
     */
    public LLCommitmentSet(PetriGame game, Transition... transitions) {
        super(transitions);
        this.game = game;
    }

    /**
     * Attention this uses just the references of the transitions of the given
     * list and the list itself. Don't change them afterward, they are saved as
     * HashSet and thus contains would not longer work!
     *
     * @param game
     * @param transitions
     */
    public LLCommitmentSet(PetriGame game, Set<Transition> transitions) {
        super(transitions);
        this.game = game;
    }

    public LLCommitmentSet(LLCommitmentSet c) {
        super(c.isTop(), c.getTransitions());
        this.game = c.game;
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
            return new LLCommitmentSet(game, true);
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
            tr.add(game.getTransition(HL2PGConverter.getTransitionID(hlID, newVal)));
        }
        return new LLCommitmentSet(game, tr);
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

    @Override
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        if (isTop()) {
            sb.append("T");
        } else {
            sb.append("{");
            for (Transition transition : getTransitions()) {
                sb.append(transition.getId()).append(",");
            }
            if (getTransitions().size() >= 1) {
                sb.delete(sb.length() - 1, sb.length());
            }
            sb.append("}");
        }
        return sb.toString();
    }
}
