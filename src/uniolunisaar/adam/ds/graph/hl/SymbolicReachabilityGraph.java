package uniolunisaar.adam.ds.graph.hl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Manuel Gieseking
 * @param <S>
 * @param <F>
 */
public class SymbolicReachabilityGraph<S extends SRGState, F extends SRGFlow> {

    private String name;
    private final Map<Integer, S> states;
    private final Set<F> flows;
    private final S initial;

    public SymbolicReachabilityGraph(String name, S initial) {
        this.name = name;
        this.states = new HashMap<>();
        this.flows = new HashSet<>();
        this.initial = initial;
        this.states.put(initial.getId(), initial);
    }

    public boolean contains(S state) {
        return states.values().contains(state);
    }

    public void addState(S state) {
        states.put(state.getId(), state);
    }

    public void addFlow(F flow) {
        flows.add(flow);
    }

    public S getState(int id) {
        return states.get(id);
    }

    public Collection<S> getStates() {
        return Collections.unmodifiableCollection(states.values());
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
