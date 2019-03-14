package uniolunisaar.adam.ds.graph.hl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniolunisaar.adam.ds.graph.State;

/**
 *
 * @author Manuel Gieseking
 * @param <S>
 * @param <F>
 */
public class SymbolicReachabilityGraph<S extends State, F extends SRGFlow> {

    private String name;
    private final Map<Integer, S> states;
    private final Set<F> flows;
    private S initial;

    public SymbolicReachabilityGraph(String name, S initial) {
        this.name = name;
        this.states = new HashMap<>();
        this.flows = new HashSet<>();
        this.initial = initial;
        this.initial.setId(0);
    }

    public S getState(int id) {
        return states.get(id);
    }

    public String getName() {
        return name;
    }

    public S getInitial() {
        return initial;
    }

}
