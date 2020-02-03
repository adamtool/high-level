package uniolunisaar.adam.logic.solver;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniolunisaar.adam.ds.graph.hl.IDecision;
import uniolunisaar.adam.ds.graph.hl.IDecisionSet;
import uniolunisaar.adam.ds.graph.hl.SGG;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public class SGGASafetyWithoutType2Solver<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC>, F extends SGGFlow<T, S>> {

    private Set<S> attractor(SGG<P, T, DC, S, F> graph, Collection<S> init, boolean p1, Map<Integer, Set<S>> distance) throws CalculationInterruptedException {
        Set<S> attr = new HashSet(init);
        Set<S> lastAdded = new HashSet<>(init);
        int i = 0;
        while (!lastAdded.isEmpty()) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            if (distance != null) {
                distance.put(i++, lastAdded);
            }
            // all predecessors of the states which had lastly been added
            Set<S> toAdd = new HashSet<>();
            for (S state : lastAdded) {
                Collection<S> predecessors = graph.getPresetView(state);
                for (S pre : predecessors) { // all predecessors
                    boolean belongsToThePlayer = (p1 && pre.isMcut()) || (!p1 && !pre.isMcut()); // it belongs to the current player
                    Collection<S> successors = graph.getPostsetView(pre);
                    boolean allInAttr = true;
                    for (S succ : successors) { /// all successors
                        if (attr.contains(succ)) { // is in the attractor
                            if (belongsToThePlayer) { // it's belongs to the current player
                                // thus one successor in the attractor is enough
                                toAdd.add(pre);
                                allInAttr = false; // just to not add it twice at the end
                                break;
                            }
                        } else {
                            allInAttr = false; // found a bad one
                            if (!belongsToThePlayer) {
                                break; // one is enough, I can stop
                            }
                        }
                    }
                    if (allInAttr) { // it's the state of the other player and all successors are already in the attractor
                        toAdd.add(pre);
                    }
                }
            }
            attr.addAll(toAdd);
            lastAdded = toAdd;
        }
        return attr;
    }

    public Set<S> winRegionSafety(SGG<P, T, DC, S, F> graph, boolean p1, Map<Integer, Set<S>> distance) throws CalculationInterruptedException {
        Set<S> attr = attractor(graph, graph.getBadStatesView(), !p1, distance);
//        System.out.println(attr.toString());
        Set<S> winning = new HashSet(graph.getStatesView());
        winning.removeAll(attr);
//        for (S s : winning) {
//            System.out.println(s.toString());
//            System.out.println("");
//        }
//        System.out.println(winning.size());
        return winning;
    }

    public boolean isWinning(SGG<P, T, DC, S, F> graph, boolean p1) throws CalculationInterruptedException {
        return winRegionSafety(graph, p1, null).contains(graph.getInitial());
    }

//    @Override
//    public BDDGraph calculateGraphGame() throws CalculationInterruptedException {
//        return BDDSGGBuilder.getInstance().builtGraph(this);
//    }
//
//    @Override
//    public BDDGraph calculateGraphStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Benchmarks.getInstance().start(Benchmarks.Parts.GRAPH_STRAT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS        
//        BDDGraph g = BDDSGGBuilder.getInstance().builtGraphStrategy(this);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
//        Benchmarks.getInstance().stop(Benchmarks.Parts.GRAPH_STRAT);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS 
//        return g;
//    }
}