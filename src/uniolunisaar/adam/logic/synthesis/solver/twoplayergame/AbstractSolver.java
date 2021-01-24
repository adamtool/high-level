package uniolunisaar.adam.logic.synthesis.solver.twoplayergame;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.synthesis.pgwt.IPetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.solver.SolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.SolvingObject;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.logic.synthesis.solver.Solver;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 * @param <G>
 * @param <SO>
 * @param <SOP>
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class AbstractSolver<W extends Condition<W>, G extends IPetriGame, SO extends SolvingObject<G, W, SO>, SOP extends SolverOptions, P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>>
        extends Solver<G, W, SO, SOP> {

//    private GameGraph<P, T, DC, S, F> graph = null;
//    private GameGraphUsingIDs<P, T, DC, S, F> graph = null;
    private AbstractGameGraph<P, T, DC, S, S, F> graph = null;

    public AbstractSolver(SO solverObject, SOP options) {
        super(solverObject, options);
    }

//    protected abstract GameGraph<P, T, DC, S, F> calculateGraph(G game);
//    protected abstract GameGraphUsingIDs<P, T, DC, S, F> calculateGraph(G game);
    protected abstract AbstractGameGraph<P, T, DC, S, S, F> calculateGraph(G game);

    protected Set<S> attractor(Collection<S> init, boolean p1, Map<Integer, Set<S>> distance) throws CalculationInterruptedException {
        Set<S> attr = new HashSet<>(init);
        Set<S> lastRound = new HashSet<>(init);
        int i = 0;
        while (!lastRound.isEmpty()) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            if (distance != null) {
                distance.put(i++, lastRound);
            }
            // all predecessors of the states already in the attractor (note: cannot only use the last added)  
            lastRound.clear();
            for (S state : attr) {
                Collection<F> predecessors = getGraph().getPresetView(state);
                for (F preFlow : predecessors) { // all predecessors
                    S pre = preFlow.getSource();
                    // if it is already in the attractor we have nothing to do
                    if (attr.contains(pre)) {
                        continue;
                    }
                    boolean belongsToThePlayer = (p1 && pre.isMcut()) || (!p1 && !pre.isMcut()); // it belongs to the current player
                    Collection<F> successors = getGraph().getPostsetView(pre);
                    boolean allInAttr = true;
                    for (F succFlow : successors) { /// all successors
                        S succ = succFlow.getTarget();
                        if (attr.contains(succ)) { // is in the attractor
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
                        lastRound.add(pre);
                    }
                }
            }
            attr.addAll(lastRound);
        }
        return attr;
    }

//    public GameGraph<P, T, DC, S, F> getGraph() {
//    public GameGraphUsingIDs<P, T, DC, S, F> getGraph() {
    public AbstractGameGraph<P, T, DC, S, S, F> getGraph() {
        if (graph == null) {
            graph = calculateGraph(getSolvingObject().getGame());
        }
        return graph;
    }

}
