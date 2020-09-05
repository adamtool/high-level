package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl;

import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class HLSolvingObject<W extends Condition<W>> extends SolvingObject<HLPetriGame, W, HLSolvingObject<W>> {

    public HLSolvingObject(HLPetriGame hlgame, W winCon) {
        super(hlgame, winCon);
    }

    public HLSolvingObject(HLSolvingObject<W> obj) {
        super(new HLPetriGame(obj.getGame()), obj.getWinCon().getCopy());
    }

    @Override
    public HLSolvingObject<W> getCopy() {
        return new HLSolvingObject<>(this);
    }
}
