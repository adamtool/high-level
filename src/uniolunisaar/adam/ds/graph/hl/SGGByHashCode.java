package uniolunisaar.adam.ds.graph.hl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Think that this method had the problems with the identifying of different
 * nodes with the same hash value. But I'm not sure!
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <S>
 * @param <DC>
 * @param <T>
 * @param <F>
 */
@Deprecated
public class SGGByHashCode<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends SGGFlow<T, IntegerID>> extends AbstractSymbolicGameGraph<P, T, DC, S, IntegerID, F> {

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
    public S getState(IntegerID id) {
        return states.get(id.getId());
    }

    @Override
    public Collection<S> getStatesView() {
        return Collections.unmodifiableCollection(states.values());
    }

    @Override
    public IntegerID getID(S state) {
        return new IntegerID(state.getId());
    }

}
