package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.GGStrategyBuilder;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class HLASafetyWithoutType2Solver<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>> extends HLSolver<Safety, P, T, DC, S, F> {

    public HLASafetyWithoutType2Solver(HLSolvingObject<Safety> solverObject, HLSolverOptions options) {
        super(solverObject, options);
    }

    public Set<S> winRegionSafety(boolean p1, Map<Integer, Set<S>> distance) throws CalculationInterruptedException {
        Set<S> attr = attractor(getGraph().getBadStates(), !p1, distance, false, null);
        // this is not true, there could be state in the winning region even though the initial state is contained in the attractor
//        Set<S> attr = attractor(getGraph().getBadStatesView(), !p1, distance, true, getGraph().getInitial());
//        if (attr == null) { // init was in the attractor
//            return new HashSet<>(); // so there is no winning region
//        }
        Set<S> winning = new HashSet<>(getGraph().getStates());
        winning.removeAll(attr);
        return winning;
    }

    public boolean isWinning(boolean p1) throws CalculationInterruptedException {
//        return winRegionSafety(p1, null).contains(getGraph().getInitial()); // don't need to do the removeAll:
        S init = getGraph().getInitial();
        Set<S> attr = attractor(getGraph().getBadStates(), !p1, null, true, init);
//        Set<S> attr = attractor(getGraph().getBadStatesView(), !p1, null, false, null);
//         Set<S> winning = new HashSet<>(getGraph().getStatesView());
//        winning.removeAll(attr);
//        for (S s : winning) {
//            System.out.println(s.toString()+"\n");
//        }
//        return !attr.contains(init);
        return attr != null;
    }

    public AbstractGameGraph<P, T, DC, S, S, F> calculateGraphStrategy(AbstractGameGraph<P, T, DC, S, S, F> graph, AbstractGameGraph<P, T, DC, S, S, F> emptyStrategy) throws CalculationInterruptedException {
//    public GameGraph<P, T, DC, S, F> calculateGraphStrategy() throws CalculationInterruptedException {
//    public GameGraphUsingIDs<P, T, DC, S, F> calculateGraphStrategy() throws CalculationInterruptedException {
        boolean p1 = false;
        GGStrategyBuilder<P, T, DC, S, F> builder = new GGStrategyBuilder<>();
        return builder.calculateGraphStrategy(getGraph(), emptyStrategy, p1, winRegionSafety(p1, null));
    }

//    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> calculateLLGraphStrategy() throws CalculationInterruptedException {
//    public abstract GameGraph<P, T, DC, S, F> calculateLLGraphStrategy() throws CalculationInterruptedException;
//    public abstract GameGraphUsingIDs<P, T, DC, S, F> calculateLLGraphStrategy() throws CalculationInterruptedException;
    public abstract AbstractGameGraph<P, T, DC, S, S, F> calculateLLGraphStrategy() throws CalculationInterruptedException;

    @Override
    protected boolean exWinStrat() throws CalculationInterruptedException {
        return isWinning(false);
    }

}
