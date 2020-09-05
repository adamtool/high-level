package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit;

import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class ExplicitSolvingObject<W extends Condition<W>> extends SolvingObject<PetriGameWithTransits, W, ExplicitSolvingObject<W>> {

    public ExplicitSolvingObject(PetriGameWithTransits game, W winCon) {
        super(game, winCon);
    }

    public ExplicitSolvingObject(ExplicitSolvingObject<W> obj) {
        super(new PetriGameWithTransits(obj.getGame()), obj.getWinCon().getCopy());
    }

    @Override
    public ExplicitSolvingObject<W> getCopy() {
        return new ExplicitSolvingObject<>(this);
    }
}
