package uniolunisaar.adam.logic.pg.solver.explicit;

import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.solver.SolvingObject;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class ExplicitSolvingObject<W extends Condition<W>> extends SolvingObject<PetriGame, W, ExplicitSolvingObject<W>> {

    public ExplicitSolvingObject(PetriGame game, W winCon) {
        super(game, winCon);
    }

    public ExplicitSolvingObject(ExplicitSolvingObject<W> obj) {
        super(new PetriGame(obj.getGame()), obj.getWinCon().getCopy());
    }

    @Override
    public ExplicitSolvingObject<W> getCopy() {
        return new ExplicitSolvingObject<>(this);
    }
}
