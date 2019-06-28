package uniolunisaar.adam.logic.hl;

import uniolunisaar.adam.symbolic.bddapproach.graph.*;
import java.util.LinkedList;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.exceptions.pg.NoStrategyExistentException;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.logic.solver.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
import uniolunisaar.adam.tools.Logger;

/**
 * @author Manuel Gieseking
 * @param <S>
 */
public class BDDSGGBuilder<S extends BDDASafetyWithoutType2HLSolver> {

    private static BDDSGGBuilder instance = null;

    public static BDDSGGBuilder getInstance() {
        if (instance == null) {
            instance = new BDDSGGBuilder();
        }
        return instance;
    }

    protected BDDSGGBuilder() {
    }

    public BDDGraph builtGraph(S solver) throws CalculationInterruptedException {
        return builtGraph(solver, false, -1);
    }

    public BDDGraph builtGraph(S solver, int depth) throws CalculationInterruptedException {
        return builtGraph(solver, false, depth);
    }

    public BDDGraph builtGraphStrategy(S solver) throws NoStrategyExistentException, CalculationInterruptedException {
        if (!solver.existsWinningStrategy()) {
            throw new NoStrategyExistentException();
        }
        return builtGraph(solver, true, -1);
    }

    /**
     *
     * @param solver
     * @param strategy
     * @param depth -1 means do the whole graph
     * @return
     */
    private BDDGraph builtGraph(S solver, boolean strategy, int depth) throws CalculationInterruptedException {
        String text = (strategy) ? "strategy" : "game";
        BDDGraph graph = new BDDGraph("Finite graph " + text + " of the net "
                + solver.getGame().getName());
        BDD states = (strategy) ? solver.getBufferedWinDCSs() : solver.getBufferedDCSs();
//        BDD states = solver.getWinDCSs();

//        try {
//            BDDTools.saveStates2Pdf("./states", states, solver);
////        states = states.not();
//        } catch (IOException ex) {
//            Logger.getLogger(BDDGraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (InterruptedException ex) {
//            Logger.getLogger(BDDGraphBuilder.class.getName()).log(Level.SEVERE, null, ex);
//        }
        LinkedList<BDDState> todoStates = new LinkedList<>();

        BDD inits = solver.getInitialDCSs();
        inits = inits.and(states);
        if (strategy) { // is strategy only add one initial state            
            addOneInitState(solver, graph, inits, todoStates);
        } else { // is graph add all initial states
            // Create a bufferstate where all initial states are childs
            BDDState in = graph.addState(solver.getOne(), solver);
            graph.setInitial(in);
            BDD init = inits.satOne(solver.getFirstBDDVariables(), false);
            while (!init.isZero()) {
                BDDState initSucc = graph.addState(init, solver);
                // mark mcut
                initSucc.setMcut(solver.isEnvState(init));
                initSucc.setBad(solver.isBadState(init));
                initSucc.setSpecial(solver.isSpecialState(init));
                graph.addFlow(in, initSucc, null);
                todoStates.add(initSucc);
                inits = inits.and(init.not());
                init = inits.satOne(solver.getFirstBDDVariables(), false);
//                System.out.println("init state");
            }
        }

        int count = 0;
        while (!todoStates.isEmpty() && depth != count) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            ++count;
            BDDState prev = todoStates.poll();
            boolean envState = prev.isMcut();
//            System.out.println("state" );
//                BDDTools.printDecodedDecisionSets(prev.getState(), solver, true);
//            System.out.println("mcut "+ envState);
//            System.out.println(!prev.getState().and(solver.getMcut()).isZero());
            BDD succs = (envState) ? solver.getEnvSuccTransitions(prev.getState()) : solver.getSystemSuccTransitions(prev.getState());

//            if (prev.getId() == 1) {
//                BDDTools.printDecisionSets(prev.getState(), true);
//                System.out.println("is env" + envState);
//                BDDTools.printDecodedDecisionSets(succs, solver, true);
//            }
            if (!succs.isZero()) {// is there a firable transition ?
//                BDDTools.printDecodedDecisionSets(succs, solver, true);
//System.out.println("TRANS");
//                BDDTools.printDecodedDecisionSets(solver.getBufferedSystemTransitions(), solver, true);
                // shift successors to the first variables
                succs = getSuccessorBDD(solver, succs, states);
//                System.out.println("succcs");
//                BDDTools.printDecodedDecisionSets(succs, solver, true);
                if (!strategy || envState) {
                    addAllSuccessors(succs, solver, graph, prev, todoStates, false);
                } else {
                    addOneSuccessor(succs, solver, graph, prev, todoStates);
                }
            }
        }
        return graph;
    }

    protected BDD getSuccessorBDD(S solver, BDD succs, BDD validStates) {
        return solver.getSymmetricStates(solver.getSuccs(succs)).and(validStates);
    }

    void addOneInitState(S solver, BDDGraph graph, BDD inits, LinkedList<BDDState> todoStates) {
        BDD init = inits.satOne(solver.getFirstBDDVariables(), false);
        BDDState in = graph.addState(init, solver);
        in.setMcut(solver.isEnvState(init));
        in.setBad(solver.isBadState(init));
        in.setSpecial(solver.isSpecialState(init));
        graph.setInitial(in);
        todoStates.add(in);
    }

    void addAllSuccessors(BDD succs, S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, boolean oneRandom) {
        BDD succ = succs.satOne(solver.getFirstBDDVariables(), false);
        while (!succ.isZero()) {
            String value = BDDTools.getDecodedDecisionSets(succ, solver);
            value = value.substring(0, value.indexOf("->"));
            addState(solver, graph, prev, todoStates, new BDDState(succ, -1, value));
            if (oneRandom) {
                return;
            }
            succs.andWith(succ.not());
            succ = succs.satOne(solver.getFirstBDDVariables(), false);
        }
    }

    void addOneSuccessor(BDD succs, S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates) {
        addAllSuccessors(succs, solver, graph, prev, todoStates, true);
    }

    protected Flow addFlow(S solver, BDDGraph graph, BDDState pre, BDDState succ) {
        return graph.addFlow(pre, succ, solver.getTransition(pre.getState(), succ.getState()));
    }

    /**
     * Tests if a state already exists in the graph, then it only adds the flow
     * otherwise it constructs the successor and the flow and adds it to the
     * todo states.
     *
     * @param solver
     * @param graph
     * @param prev
     * @param todoStates
     * @param succ
     */
    void addState(S solver, BDDGraph graph, BDDState prev, LinkedList<BDDState> todoStates, BDDState succ) {
        BDDState oldSuccState = graph.contains(succ.getState());
        if (oldSuccState != null) { // jump to every already visited cut
            addFlow(solver, graph, prev, oldSuccState);
        } else {
            BDDState succState = graph.addState(succ);
            succState.setMcut(solver.isEnvState(succ.getState()));
            succState.setBad(solver.isBadState(succ.getState()));
            succState.setSpecial(solver.isSpecialState(succ.getState()));
            addFlow(solver, graph, prev, succState);
            // take the next step
            todoStates.add(succState);
        }
    }

}
