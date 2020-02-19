package uniolunisaar.adam.logic.pg.solver.hl.hlapproach;

import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.IHLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
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
public class HLSolverFactoryHLApproach extends AbstractHLSolverFactory<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> {

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
    protected HLASafetyWithoutType2SolverHLApproach getASafetySolver(SolvingObject<HLPetriGame, Safety> obj, HLSolverOptions opts) throws SolvingException {
        return new HLASafetyWithoutType2SolverHLApproach((HLSolvingObject<Safety>) obj, opts);
    }

}
