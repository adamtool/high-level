package uniolunisaar.adam.logic.pg.solver.hl.llapproach;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.SGG;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.graph.hl.approachLL.ILLDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLDecisionSet;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.pg.solver.hl.HLASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolvingObject;

/**
 *
 * @author Manuel Gieseking
 */
public class HLASafetyWithoutType2SolverLLApproach extends HLASafetyWithoutType2Solver<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> {

    public HLASafetyWithoutType2SolverLLApproach(HLSolvingObject<Safety> solverObject, HLSolverOptions options) {
        super(solverObject, options);
    }

    @Override
    protected SGG<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> calculateGraph(HLPetriGame hlgame) {
        return SGGBuilderLL.getInstance().create(hlgame);
    }

}
