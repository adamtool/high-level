package uniolunisaar.adam.ds.graph.synthesis.twoplayergame;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This method had the problems with the identifying of different nodes with the
 * same hash value. Hash values are not meant to be unique, just a helper for
 * finding elements in data structures fast and then checking with is equal.
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <S>
 * @param <DC>
 * @param <T>
 * @param <F>
 */
@Deprecated
public class GameGraphByHashCode<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, IntegerID>> extends AbstractGameGraph<P, T, DC, S, IntegerID, F> {

    private final Map<Integer, S> states;

    public GameGraphByHashCode(String name, S initial) {
        super(name, initial);
        this.states = new HashMap<>();
        this.states.put(initial.hashCode(), initial);
    }

    @Override
    public boolean contains(S state) {
        return states.containsKey(state.hashCode());
//        return states.values().contains(state);
    }

    @Override
    public boolean containsExistingState(S state) {
        return contains(state);
    }

    @Override
    public void addState(S state) {
        states.put(state.hashCode(), state);
    }

    @Override
    public void addFreshState(S state) {
        addState(state);
    }

    @Override
    public S getState(IntegerID id) {
        return states.get(id.getId());
    }

    @Override
    public S getCorrespondingState(S state) {
        return state;
    }

    /**
     * Attention: don't change the set when using this method
     *
     * @return
     */
    @Override
    public Set<S> getStates() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Attention: don't change the set when using this method
     *
     * @return
     */
    @Override
    public Set<S> getBadStates() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<S> getStatesView() {
        return Collections.unmodifiableCollection(states.values());
    }

    @Override
    public IntegerID getID(S state) {
        return new IntegerID(state.hashCode());
    }

    @Override
    public Collection<F> getPostsetView(S state) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<F> getPresetView(S state) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<S> getBadStatesView() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
