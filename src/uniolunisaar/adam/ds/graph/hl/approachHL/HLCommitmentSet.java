package uniolunisaar.adam.ds.graph.hl.approachHL;

import java.util.HashSet;
import java.util.Set;
import uniolunisaar.adam.ds.graph.hl.CommitmentSet;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 */
public class HLCommitmentSet extends CommitmentSet<ColoredTransition> {

    public HLCommitmentSet(boolean isTop) {
        super(isTop);
    }

    public HLCommitmentSet(ColoredTransition... transitions) {
        super(transitions);
    }

    public HLCommitmentSet(Set<ColoredTransition> transitions) {
        super(transitions);
    }

    public HLCommitmentSet(HLCommitmentSet c) {
        isTop = c.isTop;
        if (isTop) {
            transitions = null;
        } else {
            transitions = new HashSet<>();
            for (ColoredTransition transition : c.transitions) {
                transitions.add(new ColoredTransition(transition));
            }
        }
    }

    @Override
    public void apply(Symmetry sym) {
        if (isTop()) {
            return;
        }
        for (ColoredTransition transition : transitions) {
            transition.apply(sym);
        }
    }
}
