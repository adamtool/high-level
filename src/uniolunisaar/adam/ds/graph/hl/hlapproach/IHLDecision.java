package uniolunisaar.adam.ds.graph.hl.hlapproach;

import uniolunisaar.adam.ds.graph.IDecision;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.petrigame.PetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public interface IHLDecision extends IDecision<ColoredPlace, ColoredTransition> {

    /**
     *
     * This method is only for the creation of the explicit graph strategy.
     *
     * @param game
     * @return
     */
    @Deprecated
    public ILLDecision toLLDecision(PetriGame game);
}
