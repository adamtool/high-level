package uniolunisaar.adam.logic.pg.builder.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Stack;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.IDecision;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.IDecisionSet;
import uniolunisaar.adam.ds.graph.StateIdentifier;
import uniolunisaar.adam.ds.petrigame.IPetriGame;

/**
 *
 * @author Manuel Gieseking
 * @param <G>
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class GameGraphBuilder<G extends IPetriGame, P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, ? extends StateIdentifier>> {

    protected <ID extends StateIdentifier> void addStatesIteratively(G game, AbstractGameGraph<P, T, DC, S, ID, GameGraphFlow<T, ID>> srg, S init,
            Collection<Transition> allTransitions, Collection<Transition> systemTransitions) {

        // Create the graph iteratively
        Stack<ID> todo = new Stack<>();
        todo.push(srg.getID(init));
        while (!todo.isEmpty()) { // as long as new states had been added        
            S state = srg.getState(todo.pop());
            // if the current state contains tops, resolve them 
            if (!state.isMcut() && state.hasTop()) {
                Set<S> succs = state.resolveTop();
                // add only the not to any existing state equivalent decision sets
                // otherwise only the flows are added to the belonging equivalent class
                addSuccessors(state, null, succs, todo, srg);
                continue;
            }

            // In mcuts only transitions having an env place in its preset are allowed to fire
            // whereas in the other states solely system transitions are valid
            Collection<Transition> transitions;
            if (state.isMcut()) {
                transitions = new ArrayList<>(allTransitions);
                transitions.removeAll(systemTransitions);
            } else {
                transitions = systemTransitions;
            }
            for (T transition : getTransitions(transitions, game)) {
                Set<S> succs = state.fire(transition);
//                    if (!state.isMcut()) {
//                        System.out.println("liko to fire" + t);
//                    }
                if (succs != null) { // had been firable
//                        if (!state.isMcut()) {
//                            System.out.println("can fire");
//                        }
                    // add only the not to any existing state equivalent decision sets
                    // otherwise only the flows are added to the belonging equivalent class
                    addSuccessors(state, transition, succs, todo, srg);
                } else {
//                        System.out.println("haven't");
                }
            }
        }
    }

    protected abstract Collection<T> getTransitions(Collection<Transition> trans, G game);

    /**
     * Adds a successor only if there is not an identical state.The
 corresponding flows are added anyways.
     *
     * @param <ID>
     * @param pre
     * @param t
     * @param succs
     * @param todo
     * @param srg
     */
    protected <ID extends StateIdentifier> void addSuccessors(S pre, T t, Set<S> succs, Stack<ID> todo, AbstractGameGraph<P, T, DC, S, ID, GameGraphFlow<T, ID>> srg) {
        for (S succ : succs) {
            ID id = srg.getID(succ);
            if (!srg.contains(succ)) {
                srg.addState(succ);
                todo.add(id);
            } else {
                id = srg.getID(succ);
            }
            srg.addFlow(new GameGraphFlow<>(srg.getID(pre), t, id));
        }
    }
}
