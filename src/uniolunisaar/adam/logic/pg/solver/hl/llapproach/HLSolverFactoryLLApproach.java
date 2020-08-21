package uniolunisaar.adam.logic.pg.solver.hl.llapproach;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.logic.pg.solver.hl.AbstractHLSolverFactory;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class HLSolverFactoryLLApproach extends AbstractHLSolverFactory<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> {

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
    protected HLASafetyWithoutType2SolverLLApproach getASafetySolver(HLPetriGame game, Safety con, HLSolverOptions opts) throws SolvingException {
        return new HLASafetyWithoutType2SolverLLApproach(createSolvingObject(game, con), opts);
    }

}
