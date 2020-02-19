package uniolunisaar.adam.ds.graph.hl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Attention: Later modification would yield inconsistent states (pre-,
 * postsets). Think of having a general class which could be used for the
 * framework and a specialized class optimized to performance gains.
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <S>
 * @param <DC>
 * @param <T>
 * @param <F>
 */
public class SGG<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends SGGFlow<T, S>> extends AbstractSymbolicGameGraph<P, T, DC, S, S, F> {

    private final Set<S> states;
    private final Set<S> badStates;
    private final Map<S, Set<S>> preSet;
    private final Map<S, Set<S>> postSet;
//    private final Set<S> V0;
//    private final Set<S> V1;

    public SGG(String name, S initial) {
        super(name, initial);
        this.states = new HashSet<>();
        this.states.add(initial);
        this.badStates = new HashSet<>();
        this.preSet = new HashMap<>();
        this.postSet = new HashMap<>();
//        this.V0 = new HashSet<>();
//        this.V1 = new HashSet<>();
    }

    @Override
    public boolean contains(S state) {
        return states.contains(state);
//        return states.values().contains(state);
    }

    @Override
    public void addState(S state) {
        if (state.isBad()) {
            badStates.add(state);
        }
//        if (state.isMcut()) {
//            V1.add(state);
//        } else {
//            V0.add(state);
//        }
        states.add(state);
    }

    @Override
    public S getState(S id) {
        return id;
    }

    /**
     * Attention: don't change the set when using this method
     *
     * @return
     */
    Set<S> getStates() {
        return states;
    }

    /**
     * Attention: don't change the set when using this method
     *
     * @return
     */
    Set<S> getBadStates() {
        return badStates;
    }

    @Override
    public Collection<S> getStatesView() {
        return Collections.unmodifiableCollection(getStates());
    }

    public Collection<S> getBadStatesView() {
        return Collections.unmodifiableCollection(getBadStates());
    }

    private Set<S> calcPostset(S state) {
        Set<S> post = new HashSet<>();
        for (F flow : getFlows()) {
            if (flow.getSource().equals(state)) {
                post.add(flow.getTarget());
            }
        }
        return post;
    }

    /**
     * Attention: don't change the set when using this method
     *
     * @param state
     * @return
     */
    Set<S> getPostset(S state) {
        Set<S> post = postSet.get(state);
        if (post == null) {
            post = calcPostset(state);
            postSet.put(state, post);
        }
        return post;
    }

    public Collection<S> getPostsetView(S state) {
        return Collections.unmodifiableCollection(getPostset(state));
    }

    private Set<S> calcPreset(S state) {
        Set<S> pre = new HashSet<>();
        for (F flow : getFlows()) {
            if (flow.getTarget().equals(state)) {
                pre.add(flow.getSource());
            }
        }
        return pre;
    }

    /**
     * Attention: don't change the set when using this method
     *
     * @param state
     * @return
     */
    Set<S> getPreset(S state) {
        Set<S> pre = preSet.get(state);
        if (pre == null) {
            pre = calcPreset(state);
            preSet.put(state, pre);
        }
        return pre;
    }

    public Collection<S> getPresetView(S state) {
        return Collections.unmodifiableCollection(getPreset(state));
    }

//
//    public Collection<S> getPlayer0States() {
//        return Collections.unmodifiableCollection(V0);
//    }
//
//    public Collection<S> getPlayer1States() {
//        return Collections.unmodifiableCollection(V1);
//    }
    @Override
    public S getID(S state) {
        return state;
    }

}
