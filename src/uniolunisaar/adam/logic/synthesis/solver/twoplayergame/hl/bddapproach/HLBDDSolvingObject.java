package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach;

import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class HLBDDSolvingObject<W extends Condition<W>> extends SolvingObject<HLPetriGame, W, HLBDDSolvingObject<W>> {

    private final DistrSysBDDSolvingObject<W> obj;

    public HLBDDSolvingObject(HLPetriGame game, W winCon) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(game, winCon);
        PetriGameWithTransits pgame = HL2PGConverter.convert(game, true, true);
        obj = new DistrSysBDDSolvingObject<>(pgame, winCon.getCopy(), true);
    }

    public HLBDDSolvingObject(HLBDDSolvingObject<W> object) {
        super(new HLPetriGame(object.getGame()), object.getWinCon().getCopy());
        this.obj = object.obj.getCopy();
    }

    @Override
    public HLBDDSolvingObject<W> getCopy() {
        return new HLBDDSolvingObject<>(this);
    }

    public DistrSysBDDSolvingObject<W> getObj() {
        return obj;
    }

}