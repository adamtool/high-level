package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl;

import java.util.Collection;
import java.util.Set;
import java.util.Stack;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.SymmetryIterator;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.StateIdentifier;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.GameGraphBuilder;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class SGGBuilder<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, ? extends StateIdentifier>> extends GameGraphBuilder<HLPetriGame, P, T, DC, S, F> {

    private Symmetries syms;

    @Override
    protected <ID extends StateIdentifier> void addStatesIteratively(HLPetriGame game, AbstractGameGraph<P, T, DC, S, ID, GameGraphFlow<T, ID>> srg, S init, Collection<Transition> allTransitions, Collection<Transition> systemTransitions) {
        syms = game.getSymmetries();
        super.addStatesIteratively(game, srg, init, allTransitions, systemTransitions);
    }

    @Override
    protected abstract Collection<T> getTransitions(Collection<Transition> trans, HLPetriGame hlgame);

    @Override
    protected <ID extends StateIdentifier> void addSuccessors(S pre, T t, Set<S> succs, Stack<ID> todo, AbstractGameGraph<P, T, DC, S, ID, GameGraphFlow<T, ID>> srg) {
        addSuccessors(pre, t, succs, syms, todo, srg);
    }

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
    <ID extends StateIdentifier> void addSuccessors(S pre, T t, Set<S> succs, Symmetries syms, Stack<ID> todo, AbstractGameGraph<P, T, DC, S, ID, GameGraphFlow<T, ID>> srg) {
        for (S succ : succs) {
            boolean newOne = true;
            S copySucc = succ;
            for (SymmetryIterator iti = syms.iterator(); iti.hasNext();) {
                Symmetry sym = iti.next();
                copySucc = succ.apply(sym);
                // note: this contains is more expensive using getValues().contains from the ID hashmap
                //       then having them directly stored in a map. But the main problem when using 
                //        this method with the GameGraphUsingIDs is that we create here a new state which does not
                //         have any id, and contains does not return the corresponding object of the graph and this 
                //          is even more expensive.
//                if (srg.contains(copySucc)) { 
                // but anyhow it's better to do it, because of the problem mentioned below. 
                // It's not good practice to have such an inconsistent state, even though it
                // can be handled with, e.g. in printing the strategy and so on.
                copySucc = srg.getCorrespondingState(copySucc);
                if (copySucc != null) {
                    newOne = false;
                    break;
                }
            }

            ID id = srg.getID(succ);
            if (newOne) {
                srg.addState(succ);
                todo.add(id);
            } else {
                // attention: this leads to not having the same instances for 
                // the successors in the flow and the states stored in the graph
                // We would have to get here the corresponding state found with 
                // the contains above to have this properly done. But this would
                // be more expensive. Cannot here just replace the old one
                // with copySucc, because then the already added flows would
                // have the problem.
                id = srg.getID(copySucc);
            }
            srg.addFlow(new GameGraphFlow<>(srg.getID(pre), t, id));
        }
    }

}
