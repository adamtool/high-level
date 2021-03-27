package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.approachBDD;

import java.util.List;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.membership.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.symbolic.bddapproach.BDDGraphAndGStrategyBuilder;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;

/**
 * Not really sure for what this class was once added (2019-04-01 19:32:53 MG:
 * added a graph builder for the symbolic graph game.)
 *
 * Because of that now deprecated.
 *
 * @author Manuel Gieseking
 */
@Deprecated
public class BDDSymbolicGraphBuilder extends BDDGraphAndGStrategyBuilder {

    private static BDDSymbolicGraphBuilder instance = null;

    public static BDDSymbolicGraphBuilder getInstance() {
        if (instance == null) {
            instance = new BDDSymbolicGraphBuilder();
        }
        return instance;
    }

    protected BDD getSuccessorBDD(BDDASafetyWithoutType2HLSolver solver, BDD succs, BDD validStates) throws CalculationInterruptedException {
        BDD symmetricSuccs = solver.getSuccs(solver.getSuccs(succs).and(solver.getSymmetries()));
        return symmetricSuccs.and(validStates);
    }

    protected Flow addFlow(BDDASafetyWithoutType2HLSolver solver, BDDGraph graph, BDDState pre, BDDState succ) {
        List<Transition> trans = solver.getAllTransitions(pre.getState(), succ.getState());
        Flow f = null;
        for (Transition tran : trans) {
            f = graph.addFlow(pre, succ, tran);
        }
        return f;
    }

}
