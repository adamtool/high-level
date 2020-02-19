package uniolunisaar.adam.logic.pg.solver.hl.llapproach;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.graph.hl.approachLL.ILLDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLDecisionSet;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.ds.solver.SolvingObject;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.logic.pg.solver.hl.AbstractHLSolverFactory;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolvingObject;

/**
 *
 * @author Manuel Gieseking
 */
public class HLSolverFactoryLLApproach extends AbstractHLSolverFactory<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> {

    private static HLSolverFactoryLLApproach instance = null;

    public static HLSolverFactoryLLApproach getInstance() {
        if (instance == null) {
            instance = new HLSolverFactoryLLApproach();
        }
        return instance;
    }

    private HLSolverFactoryLLApproach() {

    }

    @Override
    protected HLASafetyWithoutType2SolverLLApproach getASafetySolver(SolvingObject<HLPetriGame, Safety> obj, HLSolverOptions opts) throws SolvingException {
        return new HLASafetyWithoutType2SolverLLApproach((HLSolvingObject<Safety>) obj, opts);
    }

}
