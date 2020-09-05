package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach;

import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractCommitmentSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLCommitmentSet;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class HLCommitmentSet extends AbstractCommitmentSet<ColoredTransition> {

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

    /**
     *
     * This method is only for the creation of the explicit graph strategy.
     *
     * @param game
     * @return
     */
    @Deprecated
    LLCommitmentSet toLLCommitmentSet(PetriGameWithTransits game) {
        if (isTop()) {
            return new LLCommitmentSet(game, true);
        }
        Set<Transition> transitions = new HashSet<>();
        for (ColoredTransition transition : getTransitions()) {
            transitions.add(game.getTransition(HL2PGConverter.getTransitionID(transition.getTransition().getId(), transition.getVal())));
        }
        return new LLCommitmentSet(game, transitions);
    }

}
