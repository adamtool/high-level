package uniolunisaar.adam.logic.pg.builder.graph.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import uniolunisaar.adam.ds.graph.IDecision;
import uniolunisaar.adam.ds.graph.IDecisionSet;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <F>
 * @param <S>
 * @param <DC>
 */
public class SGGStrategyBuilder<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>> {

    public GameGraph<P, T, DC, S, F> calculateGraphStrategy(GameGraph<P, T, DC, S, F> graph, boolean p1, Set<S> winningRegion) throws CalculationInterruptedException {
        S init = graph.getInitial();
        GameGraph<P, T, DC, S, F> strat = new GameGraph<>(graph.getName() + "_HLstrat", init);
        LinkedList<S> added = new LinkedList<>();
        added.add(init);
        while (!added.isEmpty()) {
            S state = added.pop();
            boolean belongsToThePlayer = (p1 && state.isMcut()) || (!p1 && !state.isMcut()); // it belongs to the current player
            Collection<S> successors = graph.getPostsetView(state);
            for (S successor : successors) {
                if (winningRegion.contains(successor) && !strat.contains(successor)) {
                    strat.addState(successor);
//                    strat.addFlow(new SGGFlow<T,F>(state.getId(), null, successor.getId()));
                    List<F> flows = getFlow(graph, state, successor, belongsToThePlayer); // todo: replace this, that is expensive
                    for (F flow : flows) {
                        strat.addFlow(flow);
                    }
                    added.push(successor);
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
