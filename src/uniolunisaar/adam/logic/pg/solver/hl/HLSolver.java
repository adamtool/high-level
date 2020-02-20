package uniolunisaar.adam.logic.pg.solver.hl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import uniolunisaar.adam.ds.graph.hl.IDecision;
import uniolunisaar.adam.ds.graph.hl.IDecisionSet;
import uniolunisaar.adam.ds.graph.hl.SGG;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.ds.solver.Solver;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.tools.Logger;

/**
 *
 * @author Manuel Gieseking
 * @param <W>
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class HLSolver<W extends Condition<W>, P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends SGGFlow<T, S>> extends Solver<HLPetriGame, W, HLSolvingObject<W>, HLSolverOptions> {

    private SGG<P, T, DC, S, F> graph = null;

    public HLSolver(HLSolvingObject<W> solverObject, HLSolverOptions options) {
        super(solverObject, options);
    }

    protected abstract SGG<P, T, DC, S, F> calculateGraph(HLPetriGame hlgame);

    Set<S> attractor(Collection<S> init, boolean p1, Map<Integer, Set<S>> distance) throws CalculationInterruptedException {
        Set<S> attr = new HashSet<>(init);
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
                Collection<S> predecessors = getGraph().getPresetView(state);
                for (S pre : predecessors) { // all predecessors
                    boolean belongsToThePlayer = (p1 && pre.isMcut()) || (!p1 && !pre.isMcut()); // it belongs to the current player
                    Collection<S> successors = getGraph().getPostsetView(pre);
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

    public SGG<P, T, DC, S, F> getGraph() {
        if (graph == null) {
            graph = calculateGraph(getSolvingObject().getGame());
        }
        return graph;
    }

}
