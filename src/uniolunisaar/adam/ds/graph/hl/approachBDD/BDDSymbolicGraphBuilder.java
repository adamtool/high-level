package uniolunisaar.adam.ds.graph.hl.approachBDD;

import java.util.List;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.Flow;
import uniolunisaar.adam.logic.solver.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraphBuilder;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDState;

/**
 * @author Manuel Gieseking
 */
public class BDDSymbolicGraphBuilder extends BDDGraphBuilder<BDDASafetyWithoutType2HLSolver> {

    private static BDDSymbolicGraphBuilder instance = null;

    public static BDDSymbolicGraphBuilder getInstance() {
        if (instance == null) {
            instance = new BDDSymbolicGraphBuilder();
        }
        return instance;
    }

    @Override
    protected BDD getSuccessorBDD(BDDASafetyWithoutType2HLSolver solver, BDD succs, BDD validStates) {
        BDD symmetricSuccs = solver.getSuccs(solver.getSuccs(succs).and(solver.getSymmetries()));
        return symmetricSuccs.and(validStates);
    }

    @Override
    protected Flow addFlow(BDDASafetyWithoutType2HLSolver solver, BDDGraph graph, BDDState pre, BDDState succ) {
        List<Transition> trans = solver.getAllTransitions(pre.getState(), succ.getState());
        Flow f = null;
        for (Transition tran : trans) {
            f = graph.addFlow(pre, succ, tran);
        }
        return f;
    }

}