package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.hlapproach;

import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.AbstractHLSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class HLSolverFactoryHLApproach extends AbstractHLSolverFactory<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> {

    private static HLSolverFactoryHLApproach instance = null;

    public static HLSolverFactoryHLApproach getInstance() {
        if (instance == null) {
            instance = new HLSolverFactoryHLApproach();
        }
        return instance;
    }

    private HLSolverFactoryHLApproach() {

    }

    @Override
    protected HLASafetyWithoutType2SolverHLApproach getASafetySolver(HLPetriGame game, Safety con, HLSolverOptions opts) throws SolvingException {
        return new HLASafetyWithoutType2SolverHLApproach(createSolvingObject(game, con), opts);
    }

}
