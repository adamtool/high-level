package uniolunisaar.adam.logic.pg.solver.hl.bddapproach;

import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public class HLBDDSolvingObject<W extends Condition<W>> extends SolvingObject<HLPetriGame, W, HLBDDSolvingObject<W>> {

    private final DistrSysBDDSolvingObject<W> obj;

    public HLBDDSolvingObject(HLPetriGame game, W winCon) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(game, winCon);
        PetriGame pgame = HL2PGConverter.convert(game, true, true);
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
