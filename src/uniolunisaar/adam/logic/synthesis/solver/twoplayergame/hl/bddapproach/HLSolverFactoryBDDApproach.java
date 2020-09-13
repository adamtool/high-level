package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.local.Buchi;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.objectives.local.Reachability;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.logic.synthesis.solver.SolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
public class HLSolverFactoryBDDApproach extends SolverFactory<HLPetriGame, BDDSolverOptions, HLBDDSolver<? extends Condition<?>>> {

    private static HLSolverFactoryBDDApproach instance = null;

    public static HLSolverFactoryBDDApproach getInstance() {
        if (instance == null) {
            instance = new HLSolverFactoryBDDApproach();
        }
        return instance;
    }

    private HLSolverFactoryBDDApproach() {

    }

    @Override
    protected HLASafetyWithoutType2SolverBDDApproach getASafetySolver(HLPetriGame game, Safety con, BDDSolverOptions opts) throws SolvingException, NotSupportedGameException, NoSuitableDistributionFoundException, InvalidPartitionException {
        try {
            return new HLASafetyWithoutType2SolverBDDApproach(createSolvingObject(game, con), opts);
        } catch (NetNotSafeException ex) {
            throw new NotSupportedGameException("Could not create solver.", ex);
        }
    }

    @Override
    public HLBDDSolver<? extends Condition<?>> getSolver(String path, BDDSolverOptions options) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public HLBDDSolver<? extends Condition<?>> getSolver(HLPetriGame game, BDDSolverOptions options) throws CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(game, Condition.Objective.A_SAFETY, options); // todo: currently there is just the A_Safety solver implemented.
    }

    @Override
    protected <W extends Condition<W>> HLBDDSolvingObject<W> createSolvingObject(HLPetriGame game, W winCon) throws NotSupportedGameException {
        try {
            return new HLBDDSolvingObject<>(game, winCon);
        } catch (NetNotSafeException | NoSuitableDistributionFoundException | InvalidPartitionException ex) {
            throw new NotSupportedGameException("Could not create solving object.", ex);
        }
    }

    @Override
    protected HLBDDSolver<? extends Condition<?>> getESafetySolver(HLPetriGame game, Safety con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HLBDDSolver<? extends Condition<?>> getEReachabilitySolver(HLPetriGame game, Reachability con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HLBDDSolver<? extends Condition<?>> getAReachabilitySolver(HLPetriGame game, Reachability con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HLBDDSolver<? extends Condition<?>> getEBuchiSolver(HLPetriGame game, Buchi con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HLBDDSolver<? extends Condition<?>> getABuchiSolver(HLPetriGame game, Buchi con, BDDSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
