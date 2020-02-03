package uniolunisaar.adam.ds.graph.hl;

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
public abstract class AbstractSymbolicGameGraph<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC>, ID extends StateIdentifier, F extends SGGFlow<T, ID>> {

    private final String name;
    private final Set<F> flows;
    private final S initial;

    public AbstractSymbolicGameGraph(String name, S initial) {
        this.name = name;
        this.flows = new HashSet<>();
        this.initial = initial;
    }

    public abstract boolean contains(S state);

    public abstract void addState(S state);

    public abstract S getState(ID id);

    public abstract Collection<S> getStatesView();

    public abstract ID getID(S state);

    public void addFlow(F flow) {
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
