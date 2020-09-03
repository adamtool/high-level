package uniolunisaar.adam.logic.pg.solver.hl.bddapproach;

import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolverOptions;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public abstract class HLBDDSolver<W extends Condition<W>> extends Solver<HLPetriGame, W, HLBDDSolvingObject<W>, BDDSolverOptions> {

    public HLBDDSolver(HLBDDSolvingObject<W> solverObject, BDDSolverOptions options) {
        super(solverObject, options);
    }

}
