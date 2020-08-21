package uniolunisaar.adam.logic.pg.solver.hl.hlapproach;

import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.logic.pg.solver.hl.AbstractHLSolverFactory;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;

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
