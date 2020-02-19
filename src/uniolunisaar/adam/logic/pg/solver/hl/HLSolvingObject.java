package uniolunisaar.adam.logic.pg.solver.hl;

import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.ds.solver.SolvingObject;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class HLSolvingObject<W extends Condition<W>> extends SolvingObject<HLPetriGame, W> {

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
