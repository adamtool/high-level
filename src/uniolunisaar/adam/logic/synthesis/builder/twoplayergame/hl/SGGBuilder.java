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
                Symmetry sym = iti.next(); // todo: get rid of the identity symmetry, just do it in this case before looping
                copySucc = succ.apply(sym);
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
            srg.addFlow(new GameGraphFlow<>(srg.getID(pre), t, id));
        }
    }

}