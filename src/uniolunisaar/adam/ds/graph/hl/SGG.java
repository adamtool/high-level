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
public class SGG<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC>, F extends SGGFlow<T, S>> extends AbstractSymbolicGameGraph<P, T, DC, S, S, F> {

    private final Set<S> states;

    public SGG(String name, S initial) {
        super(name, initial);
        this.states = new HashSet<>();
        this.states.add(initial);
    }

    @Override
    public boolean contains(S state) {
        return states.contains(state);
//        return states.values().contains(state);
    }

    @Override
    public void addState(S state) {
        states.add(state);
    }

    @Override
    public S getState(S id) {
        return id;
    }

    @Override
    public Collection<S> getStates() {
        return Collections.unmodifiableCollection(states);
    }

    @Override
    public S getID(S state) {
        return state;
    }

}
