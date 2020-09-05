package uniolunisaar.adam.logic.pg.solver.hl.hlapproach;

import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.pg.builder.petrigame.HLPGStrategyBuilder;
import uniolunisaar.adam.logic.pg.converter.hl.HLSGStrat2Graphstrategy;
import uniolunisaar.adam.logic.pg.solver.hl.HLASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolvingObject;

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
