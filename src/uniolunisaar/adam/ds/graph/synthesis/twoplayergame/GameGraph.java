package uniolunisaar.adam.ds.graph.synthesis.twoplayergame;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ToDo: Merge this class with the Graph in module PetriGames.
 *
 * Attention: Later modification would yield inconsistent states (pre-,
 * postsets). Think of having a general class which could be used for the
 * framework and a specialized class optimized to performance gains.
 *
 * This seems to be very slow when the equals or hashcode functions of the
 * states are slow. Especially calculatePre and Postset is then very expensive.
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <S>
 * @param <DC>
 * @param <T>
 * @param <F>
 */
public class GameGraph<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>> extends AbstractGameGraph<P, T, DC, S, S, F> {

    private final Set<S> states;
    private final Set<S> badStates;
//    private final Map<S, Set<S>> preSet;
//    private final Map<S, Set<S>> postSet;
    // better safe the flows
    private final Map<S, Set<F>> preSet;
    private final Map<S, Set<F>> postSet;
//    private final Set<S> V0;
//    private final Set<S> V1;

    public GameGraph(String name, S initial) {
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

    @Override
    public S getCorrespondingState(S state) {
        return state;
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

    @Override
    public Collection<S> getBadStatesView() {
        return Collections.unmodifiableCollection(getBadStates());
    }

    @Deprecated
    private Set<S> calcPostset(S state) {
        Set<S> post = new HashSet<>();
        for (F flow : getFlows()) {
            if (flow.getSource().equals(state)) {
                post.add(flow.getTarget());
            }
        }
        return post;
    }

    private Set<F> calculatePostset(S state) {
        Set<F> post = new HashSet<>();
        for (F flow : getFlows()) {
            if (flow.getSource().equals(state)) {  // this is very expensive, because really all members have to be checked             
//            if (flow.getSource().getId() == state.getId()) { // this for the id methods using hashcode also
                post.add(flow);
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
    Set<F> getPostset(S state) {
        Set<F> post = postSet.get(state);
        if (post == null) {
            post = calculatePostset(state);
            postSet.put(state, post);
        }
        return post;
    }

    @Override
    public Collection<F> getPostsetView(S state) {
        return Collections.unmodifiableCollection(getPostset(state));
    }

    @Deprecated
    private Set<S> calcPreset(S state) {
        Set<S> pre = new HashSet<>();
        for (F flow : getFlows()) {
            if (flow.getTarget().equals(state)) {
                pre.add(flow.getSource());
            }
        }
        return pre;
    }

    private Set<F> calculatePreset(S state) {
        Set<F> pre = new HashSet<>();
        for (F flow : getFlows()) {
            if (flow.getTarget().equals(state)) { // this is very expensive, because really all members have to be checked
//            if (flow.getTarget().getId() == state.getId()) {// this for the id methods using hashcode also
                pre.add(flow);
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
    Set<F> getPreset(S state) {
        Set<F> pre = preSet.get(state);
        if (pre == null) {
            pre = calculatePreset(state);
            preSet.put(state, pre);
        }
        return pre;
    }

    @Override
    public Collection<F> getPresetView(S state) {
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
