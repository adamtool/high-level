package uniolunisaar.adam.ds.graph.synthesis.twoplayergame;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

/**
 * ToDo: Merge this class with the Graph in module PetriGames.Attention: Later
 * modification would yield inconsistent states (pre-, postsets).
 *
 * Think of having a general class which could be used for the framework and a
 * specialized class optimized to performance gains.
 *
 * Note: be aware that you can only use the ids of the decision sets after using
 * the addState method here. As well as all methods depending on the id of the
 * state.
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <S>
 * @param <DC>
 * @param <T>
 * @param <F>
 */
public class GameGraphUsingIDsBidiMap<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>> extends AbstractGameGraph<P, T, DC, S, S, F> {

    private final BidiMap<Integer, S> states;
    private final Set<S> badStates;
    private final Map<Integer, Set<F>> preSet;
    private final Map<Integer, Set<F>> postSet;

    private int idCounter = 0;

    public GameGraphUsingIDsBidiMap(String name, S initial) {
        super(name, initial);
        initial.overwriteId(idCounter++);
        this.states = new DualHashBidiMap<>();
        this.states.put(initial.getId(), initial);
        this.badStates = new HashSet<>();
        this.preSet = new HashMap<>();
        this.preSet.put(initial.getId(), new HashSet<>());
        this.postSet = new HashMap<>();
        this.postSet.put(initial.getId(), new HashSet<>());
    }

    public boolean contains(int id) {
        return states.containsKey(id);
    }

    @Override
    public boolean contains(S state) {
        return states.containsValue(state);
    }

    @Override
    public boolean containsExistingState(S state) {
        return contains(state.getId());
    }

    /**
     * This method can be used for the strategies to use a State and overwrite
     * its ID.
     *
     * @param state
     */
    @Override
    public void addFreshState(S state) {
        state.overwriteId(idCounter++);
        addStateWithID(state);
    }

    /**
     * This also sets an ID to the state.
     *
     * @param state
     */
    @Override
    public void addState(S state) {
        state.setId(idCounter++);
        addStateWithID(state);
    }

    private void addStateWithID(S state) {
        if (state.isBad()) {
            badStates.add(state);
        }
        this.preSet.put(state.getId(), new HashSet<>());
        this.postSet.put(state.getId(), new HashSet<>());
        states.put(state.getId(), state);
    }

    public S getState(int id) {
        return states.get(id);
    }

    @Override
    public S getState(S state) {
        return states.get(state.getId());
    }

    @Override
    public S getCorrespondingState(S state) {
        return states.get(states.getKey(state));
    }

    @Override
    public void addFlow(F flow) {
        super.addFlow(flow);
        // additionally automatically add it to the pre- and postsets
        Set<F> pre = preSet.get(flow.getTarget().getId());
        if (pre == null) {
            pre = new HashSet<>();
        }
        pre.add(flow);
        Set<F> post = postSet.get(flow.getSource().getId());
        if (post == null) {
            post = new HashSet<>();
        }
        post.add(flow);
    }

    /**
     * Attention: don't change the set when using this method
     *
     * @return
     */
    @Override
    public Collection<S> getStates() {
        return states.values();
    }

    /**
     * Attention: don't change the set when using this method
     *
     * @return
     */
    @Override
    public Set<S> getBadStates() {
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

    @Deprecated
    private Set<F> calculatePostset(S state) {
        Set<F> post = new HashSet<>();
        for (F flow : getFlows()) {
//            if (flow.getSource().equals(state)) {
            if (flow.getSource().getId() == state.getId()) {// since we have here IDs, this should be a cheap comparison                 
                post.add(flow);
            }
        }
        return post;
    }

//    /**
//     * Attention: don't change the set when using this method
//     *
//     * @param state
//     * @return
//     */
//    Set<F> getPostset(S state) {
//        Set<F> post = postSet.get(state.getId());
//        if (post == null) {
//            post = calculatePostset(state);
//            postSet.put(state.getId(), post);
//        }
//        return post;
//    }
    /**
     * Attention: don't change the set when using this method
     *
     * @param state
     * @return
     */
    public Set<F> getPostset(S state) {
        return postSet.get(state.getId());
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

    @Deprecated
    private Set<F> calculatePreset(S state) {
        Set<F> pre = new HashSet<>();
        for (F flow : getFlows()) {
//            if (flow.getTarget().equals(state)) {
            if (flow.getTarget().getId() == state.getId()) {// since we have here IDs, this should be a cheap comparison               
                pre.add(flow);
            }
        }
        return pre;
    }
//
//    /**
//     * Attention: don't change the set when using this method
//     *
//     * @param state
//     * @return
//     */
//    Set<F> getPreset(S state) {
//        Set<F> pre = preSet.get(state.getId());
//        if (pre == null) {
//            pre = calculatePreset(state);
//            preSet.put(state.getId(), pre);
//        }
//        return pre;
//    }

    /**
     * Attention: don't change the set when using this method
     *
     * @param state
     * @return
     */
    public Set<F> getPreset(S state) {
        return preSet.get(state.getId());
    }

    @Override
    public Collection<F> getPresetView(S state) {
        return Collections.unmodifiableCollection(getPreset(state));
    }

    @Override
    public S getID(S state) {
        return state;
    }

}
