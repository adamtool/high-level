package uniolunisaar.adam.logic.pg.converter.hl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.IDecision;
import uniolunisaar.adam.ds.graph.IDecisionSet;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * Here only high-level strategies can be used which have only reduced the
 * number of states by the symmetries and not additionally the number of edges!
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class AbstractSGStrat2Graphstrategy<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>> {

    abstract F createFlow(S pre, T transition, S succ);

    /**
     *
     *
     * @param hlgame
     * @param hlstrat
     * @return
     */
//    public <P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, ID extends StateIdentifier, F extends GameGraphFlow<T, ID>, DCout extends IDecision<Place, Transition>, Sout extends IDecisionSet<Place, Transition, DCout, Sout>, Fout extends GameGraphFlow<Transition, ID>>
//            GameGraph<Place, Transition, DCout, Sout, GameGraphFlow<Transition, Sout>> builtStrategy(PetriGame game, GameGraph<P, T, DC, S, GameGraphFlow<T, S>> strategy) {
    public GameGraph<P, T, DC, S, F> builtStrategy(HLPetriGame hlgame, GameGraph<P, T, DC, S, F> hlstrat) {
        Map<S, Pair<S, Symmetry>> stateMapping = new HashMap<>(); // mappes D^L -> D^H
        LinkedList<S> todoStates = new LinkedList<>(); // states D^L which still have to be processed

        Symmetries syms = hlgame.getSymmetries();

        S initHL = hlstrat.getInitial();
        GameGraph<P, T, DC, S, F> strategy = new GameGraph<>("Low-Level strategy of " + hlstrat.getName(), initHL);
        stateMapping.put(strategy.getInitial(), new Pair<>(initHL, null));
        todoStates.push(strategy.getInitial());

        while (!todoStates.isEmpty()) {
            S currentLL = todoStates.poll();
            Pair<S, Symmetry> pair = stateMapping.get(currentLL);
            S currentHL = pair.getFirst();
            Symmetry currentSymmetry = pair.getSecond();
            if (currentHL.isMcut()) { // is mcut
                // Add all successor edges
                for (F f : hlstrat.getPostsetView(currentHL)) {
//                    T t = applySymmmetry(f.getTransition(), currentSymmetry); // since all are available in the strategy (mcut) no need to add the symmetry
                    T t = f.getTransition();
                    Set<S> successorsLL = currentLL.fire(t);
                    // since it is an mcut we have exactly one successor per edge (since we also not use the reduction of the transitions)
                    S succLL = successorsLL.iterator().next();
                    Symmetry sym = findCorrespondingSymmetry(succLL, f.getTarget(), syms);
                    addSuccessor(strategy, currentLL, t, new Pair<>(succLL, sym), f.getTarget(), todoStates, stateMapping);
                }
            } else { // is system decision set
                for (F f : hlstrat.getPostsetView(currentHL)) {
                    Set<S> successorsLL;
                    T t = f.getTransition();
                    if (t == null) { // it's a top resolution
                        successorsLL = currentLL.resolveTop();
                    } else { // it's a system state successor
                        t = applySymmmetry(t, currentSymmetry, hlstrat);
                        successorsLL = currentLL.fire(t);
                    }
                    if (successorsLL == null) {
                        System.out.println(currentLL.toString());
                        System.out.println("transitoin" + t);
                        System.out.println("Flow: ");
                        System.out.println(f.toString());
                    }
                    Pair<S, Symmetry> succLL = findCorrespondingSuccessor(successorsLL, f.getTarget(), syms);
                    addSuccessor(strategy, currentLL, t, succLL, f.getTarget(), todoStates, stateMapping);
                }
            }
        }

        return strategy;
    }

    private void addSuccessor(GameGraph<P, T, DC, S, F> strategy, S pre, T t, Pair<S, Symmetry> postPair, S hlPost, LinkedList<S> todoStates, Map<S, Pair<S, Symmetry>> stateMapping) {
        S post = postPair.getFirst();
        if (!strategy.contains(post)) { // if we not already added the state
            todoStates.push(post);
            stateMapping.put(post, new Pair<>(hlPost, postPair.getSecond()));
            strategy.addState(post);
        }
        // add the edge
        strategy.addFlow(createFlow(pre, t, post));
    }

    private Pair<S, Symmetry> findCorrespondingSuccessor(Set<S> succsLL, S succHL, Symmetries syms) {
        for (Symmetry sym : syms) {
            S symState = succHL.apply(sym);
            if (succsLL.contains(symState)) {
                return new Pair<>(symState, sym);
            }
        }
        throw new RuntimeException("Could not find a corresponding symmetric successor for " + succHL.toString() + "in " + succsLL.toString() + ". This should never happen!");
    }

    private Symmetry findCorrespondingSymmetry(S succLL, S succHL, Symmetries syms) {
        for (Symmetry sym : syms) {
            S symState = succHL.apply(sym);
            if (succLL.equals(symState)) {
                return sym;
            }
        }
        throw new RuntimeException("Could not find a corresponding symmetry for " + succHL.toString() + " and " + succLL.toString() + ". This should never happen!");
    }

    abstract T applySymmmetry(T t, Symmetry currentSymmetry, GameGraph<P, T, DC, S, F> hlstrat);

}
