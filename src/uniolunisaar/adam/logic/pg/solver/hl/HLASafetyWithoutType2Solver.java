package uniolunisaar.adam.logic.pg.solver.hl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniolunisaar.adam.ds.graph.hl.IDecision;
import uniolunisaar.adam.ds.graph.hl.IDecisionSet;
import uniolunisaar.adam.ds.graph.hl.SGG;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGStrategyBuilder;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class HLASafetyWithoutType2Solver<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC>, F extends SGGFlow<T, S>> extends HLSolver<Safety, P, T, DC, S, F> {

    public HLASafetyWithoutType2Solver(HLSolvingObject<Safety> solverObject, HLSolverOptions options) {
        super(solverObject, options);
    }

    public Set<S> winRegionSafety(boolean p1, Map<Integer, Set<S>> distance) throws CalculationInterruptedException {
        Set<S> attr = attractor(getGraph().getBadStatesView(), !p1, distance);
//        System.out.println(attr.toString());
        Set<S> winning = new HashSet(getGraph().getStatesView());
        winning.removeAll(attr);
//        for (S s : winning) {
//            System.out.println(s.toString());
//            System.out.println("");
//        }
//        System.out.println(winning.size());
        return winning;
    }

    public boolean isWinning(boolean p1) throws CalculationInterruptedException {
        return winRegionSafety(p1, null).contains(getGraph().getInitial());
    }

    public SGG<P, T, DC, S, F> calculateGraphStrategy(SGG<P, T, DC, S, F> graph, boolean p1) throws CalculationInterruptedException {
        SGGStrategyBuilder<P, T, DC, S, F> builder = new SGGStrategyBuilder<>();
        return builder.calculateGraphStrategy(getGraph(), p1, winRegionSafety(p1, null));
    }

    @Override
    protected boolean exWinStrat() throws CalculationInterruptedException {
        return isWinning(true);
    }

    @Override
    protected PetriGame calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
