package uniolunisaar.adam.logic.pg.solver.hl.bddapproach;

import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class HLASafetyWithoutType2SolverBDDApproach extends HLBDDSolver<Safety> {

    private final BDDASafetyWithoutType2HLSolver solver;

    public HLASafetyWithoutType2SolverBDDApproach(HLBDDSolvingObject<Safety> solverObject, BDDSolverOptions options) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(solverObject, options);
        Symmetries syms = new Symmetries(solverObject.getGame().getBasicColorClasses());
        solver = new BDDASafetyWithoutType2HLSolver(solverObject.getObj(), syms, options);
    }

    @Override
    protected boolean exWinStrat() throws CalculationInterruptedException {
        return solver.existsWinningStrategy();
    }

    public BDDGraph calculateGraphGame() throws CalculationInterruptedException {
        return solver.calculateGraphGame();
    }

    public BDDGraph calculateGraphStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return solver.calculateGraphStrategy();
    }

    @Override
    protected PetriGame calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return solver.calculateStrategy();
    }

}
