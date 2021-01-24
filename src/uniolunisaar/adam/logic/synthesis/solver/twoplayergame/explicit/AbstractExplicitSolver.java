package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit;

import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphUsingIDs;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.GGStrategyBuilder;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.explicit.GGBuilder;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.AbstractSolver;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public abstract class AbstractExplicitSolver<W extends Condition<W>>
        extends AbstractSolver<W, PetriGameWithTransits, ExplicitSolvingObject<W>, ExplicitSolverOptions, Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> {

    public AbstractExplicitSolver(ExplicitSolvingObject<W> solverObject, ExplicitSolverOptions options) {
        super(solverObject, options);
    }

    abstract Set<DecisionSet> winningRegion(boolean p1, Map<Integer, Set<DecisionSet>> distance) throws CalculationInterruptedException;

    public boolean isWinning(boolean p1) throws CalculationInterruptedException {
        return winningRegion(p1, null).contains(getGraph().getInitial());
    }

//    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraphStrategy() throws CalculationInterruptedException {
    public GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraphStrategy() throws CalculationInterruptedException {
        boolean p1 = false;
        GGStrategyBuilder<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> builder = new GGStrategyBuilder<>();
        return builder.calculateGraphStrategy(getGraph(), p1, winningRegion(p1, null));
    }

    @Override
//    protected GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraph(PetriGameWithTransits game) {
    protected GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraph(PetriGameWithTransits game) {
        return GGBuilder.getInstance().create(game);
    }

    @Override
    protected boolean exWinStrat() throws CalculationInterruptedException {
        return isWinning(false);
    }
}
