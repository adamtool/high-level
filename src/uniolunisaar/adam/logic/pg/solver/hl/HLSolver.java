package uniolunisaar.adam.logic.pg.solver.hl;

import uniolunisaar.adam.ds.graph.IDecision;
import uniolunisaar.adam.ds.graph.IDecisionSet;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.logic.pg.solver.AbstractSolver;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class HLSolver<W extends Condition<W>, P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>>
        extends AbstractSolver<W, HLPetriGame, HLSolvingObject<W>, HLSolverOptions, P, T, DC, S, F> {

    public HLSolver(HLSolvingObject<W> solverObject, HLSolverOptions options) {
        super(solverObject, options);
    }

}
