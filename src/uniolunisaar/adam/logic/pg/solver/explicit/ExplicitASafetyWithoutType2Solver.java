package uniolunisaar.adam.logic.pg.solver.explicit;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.logic.pg.builder.petrigame.LLPGStrategyBuilder;

/**
 *
 * @author Manuel Gieseking
 */
public class ExplicitASafetyWithoutType2Solver extends AbstractExplicitSolver<Safety> {

    public ExplicitASafetyWithoutType2Solver(ExplicitSolvingObject<Safety> solverObject, ExplicitSolverOptions options) {
        super(solverObject, options);
    }

    @Override
    Set<DecisionSet> winningRegion(boolean p1, Map<Integer, Set<DecisionSet>> distance) throws CalculationInterruptedException {
        Set<DecisionSet> attr = attractor(getGraph().getBadStatesView(), !p1, distance);
        Set<DecisionSet> winning = new HashSet<>(getGraph().getStatesView());
        winning.removeAll(attr);
        return winning;
    }

    @Override
    protected PetriGame calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return LLPGStrategyBuilder.getInstance().builtStrategy(getGame(), calculateGraphStrategy());
    }

}
