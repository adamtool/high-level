package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.llapproach;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphUsingIDsBidiMap;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.synthesis.builder.pgwt.highlevel.LLPGStrategyBuilder;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.LLSGStrat2Graphstrategy;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolvingObject;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 */
public class HLASafetyWithoutType2SolverLLApproach extends HLASafetyWithoutType2Solver<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> {

    public HLASafetyWithoutType2SolverLLApproach(HLSolvingObject<Safety> solverObject, HLSolverOptions options) {
        super(solverObject, options);
    }

    @Override
//    protected GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraph(HLPetriGame hlgame) {
    protected AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet,DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraph(HLPetriGame hlgame) {
//    protected GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraph(HLPetriGame hlgame) {
        return SGGBuilderLL.getInstance().create(hlgame);
    }

    public AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateGraphStrategy() throws CalculationInterruptedException {
        DecisionSet init = getGraph().getInitial();// Create the initial state
//        GameGraph<P, T, DC, S, F> strat = new GameGraph<>(graph.getName() + "_HLstrat", init);
//        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> strat = new GameGraph<>(getGraph().getName() + "_HLstrat", init);
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> strat = new GameGraphUsingIDsBidiMap<>(getGraph().getName() + "_HLstrat", init);

        return super.calculateGraphStrategy(getGraph(), strat);
    }

    @Override
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return LLPGStrategyBuilder.getInstance().builtStrategy(getGame().getName(), calculateLLGraphStrategy());
    }

    @Override
//    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateLLGraphStrategy() throws CalculationInterruptedException {
//    public GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateLLGraphStrategy() throws CalculationInterruptedException {
    public AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateLLGraphStrategy() throws CalculationInterruptedException {
        DecisionSet init = getGraph().getInitial();// Create the initial state
//        GameGraph<P, T, DC, S, F> strat = new GameGraph<>(graph.getName() + "_HLstrat", init);
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> strat = new GameGraph<>(getGraph().getName() + "_HLstrat", init);

        return LLSGStrat2Graphstrategy.getInstance().builtStrategy(getSolvingObject().getGame(), calculateGraphStrategy(getGraph(), strat));
    }
    
    
     /**
     * The same as the super method, but just using the ids of the states.
     *
     * @param init
     * @param p1
     * @param distance
     * @return
     * @throws CalculationInterruptedException
     */
    @Override
    protected Set<DecisionSet> attractor(Collection<DecisionSet> init, boolean p1, Map<Integer, Set<DecisionSet>> distance) throws CalculationInterruptedException {
        GameGraphUsingIDsBidiMap<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graph = (GameGraphUsingIDsBidiMap<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>>) getGraph();
        Set<Integer> attr = new HashSet<>();
        Set<Integer> lastRound = new HashSet<>();
        for (DecisionSet in : init) {
            attr.add(in.getId());
            lastRound.add(in.getId());
        }
        int i = 0;
        while (!lastRound.isEmpty()) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            // todo: currently no distance support
//            if (distance != null) {
//                distance.put(i++, lastRound);
//            }
            // all predecessors of the states already in the attractor (note: cannot only use the last added)  
            lastRound.clear();
            for (Integer id : attr) {
                DecisionSet state = graph.getState(id);
                Collection<GameGraphFlow<Transition, DecisionSet>> predecessors = getGraph().getPresetView(state);
                for (GameGraphFlow<Transition, DecisionSet> preFlow : predecessors) { // all predecessors
                    DecisionSet pre = preFlow.getSource();
                    // if it is already in the attractor we have nothing to do
                    if (attr.contains(pre.getId())) {
                        continue;
                    }
                    boolean belongsToThePlayer = (p1 && pre.isMcut()) || (!p1 && !pre.isMcut()); // it belongs to the current player
                    Collection<GameGraphFlow<Transition, DecisionSet>> successors = getGraph().getPostsetView(pre);
                    boolean allInAttr = true;
                    for (GameGraphFlow<Transition, DecisionSet> succFlow : successors) { /// all successors
                        DecisionSet succ = succFlow.getTarget();
                        if (attr.contains(succ.getId())) { // is in the attractor
                            if (belongsToThePlayer) { // it's belongs to the current player
                                // thus one successor in the attractor is enough
                                // and pre is already the good one
                                break;
                            }
                        } else {
                            allInAttr = false; // found a bad one
                            if (!belongsToThePlayer) {
                                break; // one is enough, I can stop
                            }
                        }
                    }
                    if (allInAttr) { // it's the state of the other player and all successors are already in the attractor
                        lastRound.add(pre.getId());
                    }
                }
            }
            attr.addAll(lastRound);
        }

        Set<DecisionSet> out = new HashSet<>();
        for (Integer id : attr) {
            out.add(graph.getState(id));
        }
        return out;
    }

}
