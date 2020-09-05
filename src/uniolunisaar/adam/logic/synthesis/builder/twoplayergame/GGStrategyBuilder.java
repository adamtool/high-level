package uniolunisaar.adam.logic.synthesis.builder.twoplayergame;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <F>
 * @param <S>
 * @param <DC>
 */
public class GGStrategyBuilder<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>> {

    public GameGraph<P, T, DC, S, F> calculateGraphStrategy(GameGraph<P, T, DC, S, F> graph, boolean p1, Set<S> winningRegion) throws CalculationInterruptedException {
        S init = graph.getInitial();// Create the initial state
        GameGraph<P, T, DC, S, F> strat = new GameGraph<>(graph.getName() + "_HLstrat", init);
        LinkedList<S> added = new LinkedList<>();
        added.add(init);
        while (!added.isEmpty()) { // as long as new successors had been added
            S state = added.pop();
            boolean belongsToThePlayer = (p1 && state.isMcut()) || (!p1 && !state.isMcut()); // it belongs to the current player
//            Collection<S> successors = graph.getPostsetView(state);
//            for (S successor : successors) {
            Collection<F> successors = graph.getPostsetView(state);
            for (F succFlow : successors) {
                S successor = succFlow.getTarget();
                if (winningRegion.contains(successor)) {
                    if (!strat.contains(successor)) {
                        strat.addState(successor);
                        strat.addFlow(succFlow);
                        added.push(successor);
                    } else {
                        strat.addFlow(succFlow);
                    }
                    if (belongsToThePlayer) {
                        break;
                    }
                }
            }
        }
        return strat;
    }

    /**
     * TOOD: do it better since it is really expensive
     *
     * @param graph
     * @param pre
     * @param post
     * @return
     */
    @Deprecated
    private List<F> getFlow(GameGraph<P, T, DC, S, F> graph, S pre, S post, boolean one) {
        List<F> flows = new ArrayList<>();
        for (F f : graph.getFlowsView()) {
            if (f.getSource().equals(pre) && f.getTarget().equals(post)) {
                flows.add(f);
                if (one) {
                    return flows;
                }
            }
        }
        return flows;
    }

}
