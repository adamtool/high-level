package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.canonicalreps;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.AbstractHLSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class HLSolverFactoryCanonApproach extends AbstractHLSolverFactory<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> {

    private static HLSolverFactoryCanonApproach instance = null;

    public static HLSolverFactoryCanonApproach getInstance() {
        if (instance == null) {
            instance = new HLSolverFactoryCanonApproach();
        }
        return instance;
    }

    private HLSolverFactoryCanonApproach() {

    }

    @Override
    protected HLASafetyWithoutType2SolverCanonApproach getASafetySolver(HLPetriGame game, Safety con, HLSolverOptions opts) throws SolvingException {
        return new HLASafetyWithoutType2SolverCanonApproach(createSolvingObject(game, con), opts);
    }

}
