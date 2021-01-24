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
        Set<S> attr = attractor(getGraph().getBadStatesView(), !p1, distance);
//        System.out.println(attr.toString());        
//        System.out.println("bad size " + getGraph().getBadStatesView().size());
//        System.out.println("attr size " + attr.size());
        Set<S> winning = new HashSet<>(getGraph().getStatesView());
//        System.out.println("graph size " + winning.size());
//        boolean changed = winning.removeAll(attr);
        winning.removeAll(attr);
//        System.out.println(changed);
//        System.out.println("winning size " + winning.size());

//        for (S s : winning) {
//            System.out.println(s.toString());
//            System.out.println("");
//        }
//        System.out.println(winning.size());
        return winning;
    }

    public boolean isWinning(boolean p1) throws CalculationInterruptedException {
//        return winRegionSafety(p1, null).contains(getGraph().getInitial()); // don't need to do the removeAll:
        Set<S> attr = attractor(getGraph().getBadStatesView(), !p1, null);
        return !attr.contains(getGraph().getInitial());
    }

    public AbstractGameGraph<P, T, DC, S, S, F> calculateGraphStrategy() throws CalculationInterruptedException {
//    public GameGraph<P, T, DC, S, F> calculateGraphStrategy() throws CalculationInterruptedException {
//    public GameGraphUsingIDs<P, T, DC, S, F> calculateGraphStrategy() throws CalculationInterruptedException {
        boolean p1 = false;
        GGStrategyBuilder<P, T, DC, S, F> builder = new GGStrategyBuilder<>();
        return builder.calculateGraphStrategy(getGraph(), p1, winRegionSafety(p1, null));
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
