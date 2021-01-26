package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.logic.synthesis.builder.pgwt.highlevel.LLPGStrategyBuilder;

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
        Set<DecisionSet> attr = attractor(getGraph().getBadStatesView(), !p1, distance, true, getGraph().getInitial());
        if (attr == null) { // init was in the attractor
            return new HashSet<>(); // so there is no winning region
        }
        Set<DecisionSet> winning = new HashSet<>(getGraph().getStatesView());
        winning.removeAll(attr);
        return winning;
    }

    @Override
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return LLPGStrategyBuilder.getInstance().builtStrategy(getGame(), calculateGraphStrategy());
    }

}
