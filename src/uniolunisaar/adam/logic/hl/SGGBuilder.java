package uniolunisaar.adam.logic.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.Stack;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.AbstractSymbolicGameGraph;
import uniolunisaar.adam.ds.graph.hl.IDecision;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.highlevel.symmetries.SymmetryIterator;
import uniolunisaar.adam.ds.graph.hl.IDecisionSet;
import uniolunisaar.adam.ds.graph.hl.StateIdentifier;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class SGGBuilder<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC>, F extends SGGFlow<T, ? extends StateIdentifier>> {

    <ID extends StateIdentifier> void addStatesIteratively(HLPetriGame hlgame, AbstractSymbolicGameGraph<P, T, DC, S, ID, SGGFlow<T, ID>> srg, S init,
            Collection<Transition> allTransitions, Collection<Transition> systemTransitions) {
        // Create the iterator for the symmetries
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        // Create the graph iteratively
        Stack<ID> todo = new Stack<>();
        todo.push(srg.getID(init));
        while (!todo.isEmpty()) { // as long as new states had been added        
            S state = srg.getState(todo.pop());
            // if the current state contains tops, resolve them 
            if (!state.isMcut() && state.hasTop()) {
                Set<S> succs = (Set<S>) state.resolveTop();
                // add only the not to any existing state equivalent decision sets
                // otherwise only the flows are added to the belonging equivalent class
                addSuccessors(state, null, succs, syms, todo, srg);
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
            for (T transition : getTransitions(transitions, hlgame)) {
                Set<S> succs = (Set<S>) state.fire(transition);
//                    if (!state.isMcut()) {
//                        System.out.println("liko to fire" + t);
//                    }
                if (succs != null) { // had been firable
//                        if (!state.isMcut()) {
//                            System.out.println("can fire");
//                        }
                    // add only the not to any existing state equivalent decision sets
                    // otherwise only the flows are added to the belonging equivalent class
                    addSuccessors(state, transition, succs, syms, todo, srg);
                } else {
//                        System.out.println("haven't");
                }
            }
        }
    }

    abstract Collection<T> getTransitions(Collection<Transition> trans, HLPetriGame hlgame);

    /**
     * Adds a successor only if there is not already any equivalence class
     * (regarding the symmetries) containing the successor. The corresponding
     * flows are added anyways.
     *
     * @param succs
     * @param syms
     * @param todo
     * @param srg
     */
    <ID extends StateIdentifier> void addSuccessors(S pre, T t, Set<S> succs, Symmetries syms, Stack<ID> todo, AbstractSymbolicGameGraph<P, T, DC, S, ID, SGGFlow<T, ID>> srg) {
        for (S succ : succs) {
            boolean newOne = true;
            S copySucc = succ;
            for (SymmetryIterator iti = syms.iterator(); iti.hasNext();) {
                Symmetry sym = iti.next(); // todo: get rid of the identity symmetry, just do it in this case before looping
                copySucc = (S) succ.apply(sym);
                if (srg.contains(copySucc)) {
                    newOne = false;
                    break;
                }
            }

            ID id = srg.getID(succ);
            if (newOne) {
                srg.addState(succ);
                todo.add(id);
            } else {
                id = srg.getID(copySucc);
            }
            srg.addFlow(new SGGFlow<>(srg.getID(pre), t, id));
        }
    }
}
