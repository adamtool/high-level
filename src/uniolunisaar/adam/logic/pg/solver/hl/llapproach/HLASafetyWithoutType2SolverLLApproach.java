package uniolunisaar.adam.logic.pg.solver.hl.llapproach;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.pg.builder.petrigame.LLPGStrategyBuilder;
import uniolunisaar.adam.logic.pg.converter.hl.LLSGStrat2Graphstrategy;
import uniolunisaar.adam.logic.pg.solver.hl.HLASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolvingObject;

/**
 *
 * @author Manuel Gieseking
 */
public class HLASafetyWithoutType2SolverLLApproach extends HLASafetyWithoutType2Solver<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> {

    public HLASafetyWithoutType2SolverLLApproach(HLSolvingObject<Safety> solverObject, HLSolverOptions options) {
        super(solverObject, options);
    }

    @Override
    protected GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraph(HLPetriGame hlgame) {
        return SGGBuilderLL.getInstance().create(hlgame);
    }

    @Override
    protected PetriGame calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return LLPGStrategyBuilder.getInstance().builtStrategy(getGame().getName(), calculateLLGraphStrategy());
    }

    @Override
    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateLLGraphStrategy() throws CalculationInterruptedException {
        return LLSGStrat2Graphstrategy.getInstance().builtStrategy(getSolvingObject().getGame(), calculateGraphStrategy());
    }

}
