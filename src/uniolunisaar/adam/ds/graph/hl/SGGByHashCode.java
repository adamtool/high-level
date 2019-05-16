package uniolunisaar.adam.ds.graph.hl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <S>
 * @param <DC>
 * @param <T>
 * @param <F>
 */
public class SGGByHashCode<P, T, DC extends IDecision<P, T>, S extends DecisionSet<P, T, DC>, F extends SRGFlow<T>> extends AbstractSymbolicGameGraph<P, T, DC, S, F> {

    private final Map<Integer, S> states;

    public SGGByHashCode(String name, S initial) {
        super(name, initial);
        this.states = new HashMap<>();
        this.states.put(initial.getId(), initial);
    }

    @Override
    public boolean contains(S state) {
        return states.containsKey(state.getId());
//        return states.values().contains(state);
    }

    @Override
    public void addState(S state) {
        states.put(state.getId(), state);
    }

    @Override
    public S getState(int id) {
        return states.get(id);
    }

    @Override
    public Collection<S> getStates() {
        return Collections.unmodifiableCollection(states.values());
    }
}
