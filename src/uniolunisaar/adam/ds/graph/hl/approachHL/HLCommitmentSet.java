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

    /**
     * Attention this uses just the references of the transitions of the given
     * list and the list itself. Don't change them afterward, they are saved as
     * HashSet and thus contains would not longer work!
     *
     * @param transitions
     */
    public HLCommitmentSet(ColoredTransition... transitions) {
        super(transitions);
    }

    /**
     * Attention this uses just the references of the transitions of the given
     * list and the list itself. Don't change them afterward, they are saved as
     * HashSet and thus contains would not longer work!
     *
     * @param transitions
     */
    public HLCommitmentSet(Set<ColoredTransition> transitions) {
        super(transitions);
    }

    public HLCommitmentSet(HLCommitmentSet c) {
        super(c.isTop(), c.getTransitions());
    }

//    @Override
//    public void apply(Symmetry sym) {
//        if (isTop()) {
//            return;
//        }
//        for (ColoredTransition transition : transitions) {
//            transition.apply(sym);
//        }
//    }
    @Override
    public HLCommitmentSet apply(Symmetry sym) {
        if (isTop()) {
            return new HLCommitmentSet(true);
        }
        Set<ColoredTransition> c = new HashSet<>();
        for (ColoredTransition transition : getTransitions()) {
            c.add(transition.apply(sym));
        }
        return new HLCommitmentSet(c);
    }

}
