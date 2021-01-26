package uniolunisaar.adam.ds.graph.synthesis.twoplayergame;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <S>
 * @param <DC>
 * @param <T>
 * @param <ID>
 * @param <F>
 */
public abstract class AbstractGameGraph<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, ID extends StateIdentifier, F extends GameGraphFlow<T, ID>> {

    private final String name;
    private final Set<F> flows;
    private final S initial;

    public AbstractGameGraph(String name, S initial) {
        this.name = name;
        this.flows = new HashSet<>();
        this.initial = initial;
    }

    public abstract boolean contains(S state);

    public abstract boolean containsExistingState(S state);

    public abstract void addState(S state);

    public abstract void addFreshState(S state);

    public abstract S getState(ID id);

    public abstract S getCorrespondingState(S state);

    public abstract Collection<S> getStatesView();

    public abstract Collection<F> getPostsetView(S state);

    public abstract Collection<F> getPresetView(S state);

    public abstract Collection<S> getBadStatesView();

    public abstract ID getID(S state);

    public void addFlow(F flow) {
//        if (flow.getTarget().getId() == -1) {
//            throw new RuntimeException(flow.toString());
////            System.out.println(flow.toString());
//        }
//        if (flow.getSource().getId() == -1) {
//            System.out.println(flow.toString());
//        }
        flows.add(flow);
    }

    /**
     * Attention: don't change the set when using this method.
     *
     * @return
     */
    Set<F> getFlows() {
        return flows;
    }

    public Collection<F> getFlowsView() {
        return Collections.unmodifiableCollection(getFlows());
    }

    public String getName() {
        return name;
    }

    public S getInitial() {
        return initial;
    }

}
