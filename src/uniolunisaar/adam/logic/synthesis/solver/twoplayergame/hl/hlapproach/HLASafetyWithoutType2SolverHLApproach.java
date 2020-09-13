package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.hlapproach;

import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.synthesis.builder.pgwt.highlevel.HLPGStrategyBuilder;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HLSGStrat2Graphstrategy;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolvingObject;

/**
 *
 * @author Manuel Gieseking
 */
public class HLASafetyWithoutType2SolverHLApproach extends HLASafetyWithoutType2Solver<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> {

    public HLASafetyWithoutType2SolverHLApproach(HLSolvingObject<Safety> solverObject, HLSolverOptions options) {
        super(solverObject, options);
    }

    @Override
    protected GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> calculateGraph(HLPetriGame hlgame) {
        return SGGBuilderHL.getInstance().create(new OneEnvHLPG(hlgame, true));
    }

    @Override
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return HLPGStrategyBuilder.getInstance().builtStrategy(getGame().getName(), calculateLLGraphStrategy());
    }

    @Override
    public GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> calculateLLGraphStrategy() throws CalculationInterruptedException {
        return HLSGStrat2Graphstrategy.getInstance().builtStrategy(getSolvingObject().getGame(), calculateGraphStrategy());
    }

}
