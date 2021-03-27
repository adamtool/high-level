package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.canonicalreps;

import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.HLBDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.HLBDDSolvingObject;

/**
 *
 * @author Manuel Gieseking
 */
public class HLASafetyWithoutType2CanonRepSolverBDDApproach extends HLBDDSolver<Safety> {

    private final BDDASafetyWithoutType2CanonRepHLSolver solver;

    public HLASafetyWithoutType2CanonRepSolverBDDApproach(HLBDDSolvingObject<Safety> solverObject, BDDSolverOptions options) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(solverObject, options);
        Iterable<Symmetry> syms = solverObject.getGame().getSymmetries();
        solver = new BDDASafetyWithoutType2CanonRepHLSolver(solverObject, syms, options);
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
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return solver.calculateStrategy();
    }

    public BDDASafetyWithoutType2CanonRepHLSolver getSolver() {
        return solver;
    }

}
