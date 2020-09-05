package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach;

import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.logic.synthesis.solver.Solver;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;

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
