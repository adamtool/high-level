package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.llapproach;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.AbstractHLSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolverOptions;

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
