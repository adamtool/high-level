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

    public LLCommitmentSet(PetriGame game, Transition... transitions) {
        super(transitions);
        this.game = game;
    }

    public LLCommitmentSet(PetriGame game, Set<Transition> transitions) {
        super(transitions);
        this.game = game;
    }

    public LLCommitmentSet(PetriGame game, LLCommitmentSet c) {
        isTop = c.isTop;
        if (isTop) {
            transitions = null;
        } else {
            // here is a copy of the references OK
            // (as long as no one uses the extensions such that it is not OK)
            transitions = new HashSet<>(c.transitions);
        }
        this.game = game;
    }

    @Override
    public void apply(Symmetry sym) {
        if (isTop()) {
            return;
        }
        Set<Transition> tr = new HashSet<>();
        for (Transition transition : transitions) {
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
        transitions = tr;
    }

    @Override
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        if (isTop) {
            sb.append("T");
        } else {
            sb.append("{");
            for (Transition transition : transitions) {
                sb.append(transition.getId()).append(",");
            }
            if (transitions.size() >= 1) {
                sb.delete(sb.length() - 1, sb.length());
            }
            sb.append("}");
        }
        return sb.toString();
    }
}
