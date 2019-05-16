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
 * @param <F>
 */
public abstract class AbstractSymbolicGameGraph<P, T, DC extends IDecision<P, T>, S extends DecisionSet<P, T, DC>, F extends SRGFlow<T>> {

    private String name;
    private final Set<F> flows;
    private final S initial;

    public AbstractSymbolicGameGraph(String name, S initial) {
        this.name = name;
        this.flows = new HashSet<>();
        this.initial = initial;
    }

    public abstract boolean contains(S state);

    public abstract void addState(S state);

    public abstract S getState(int id);

    public abstract Collection<S> getStates();

    public void addFlow(F flow) {
        flows.add(flow);
    }

    public Collection<F> getFlows() {
        return Collections.unmodifiableCollection(flows);
    }

    public String getName() {
        return name;
    }

    public S getInitial() {
        return initial;
    }

}
