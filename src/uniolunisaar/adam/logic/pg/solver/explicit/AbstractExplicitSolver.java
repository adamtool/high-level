package uniolunisaar.adam.logic.pg.solver.explicit;

import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.logic.pg.builder.graph.GGStrategyBuilder;
import uniolunisaar.adam.logic.pg.builder.graph.explicit.GGBuilder;
import uniolunisaar.adam.logic.pg.solver.AbstractSolver;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 */
public abstract class AbstractExplicitSolver<W extends Condition<W>>
        extends AbstractSolver<W, PetriGame, ExplicitSolvingObject<W>, ExplicitSolverOptions, Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> {

    public AbstractExplicitSolver(ExplicitSolvingObject<W> solverObject, ExplicitSolverOptions options) {
        super(solverObject, options);
    }

    abstract Set<DecisionSet> winningRegion(boolean p1, Map<Integer, Set<DecisionSet>> distance) throws CalculationInterruptedException;

    public boolean isWinning(boolean p1) throws CalculationInterruptedException {
        return winningRegion(p1, null).contains(getGraph().getInitial());
    }

    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraphStrategy() throws CalculationInterruptedException {
        boolean p1 = false;
        GGStrategyBuilder<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> builder = new GGStrategyBuilder<>();
        return builder.calculateGraphStrategy(getGraph(), p1, winningRegion(p1, null));
    }

    @Override
    protected GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraph(PetriGame game) {
        return GGBuilder.getInstance().create(game);
    }

    @Override
    protected boolean exWinStrat() throws CalculationInterruptedException {
        return isWinning(true);
    }
}
