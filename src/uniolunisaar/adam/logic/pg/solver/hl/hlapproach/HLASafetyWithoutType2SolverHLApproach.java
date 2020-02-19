package uniolunisaar.adam.logic.pg.solver.hl.hlapproach;

import uniolunisaar.adam.ds.graph.hl.SGG;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.IHLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.pg.solver.hl.HLASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolvingObject;

/**
 *
 * @author Manuel Gieseking
 */
public class HLASafetyWithoutType2SolverHLApproach extends HLASafetyWithoutType2Solver<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> {

    public HLASafetyWithoutType2SolverHLApproach(HLSolvingObject<Safety> solverObject, HLSolverOptions options) {
        super(solverObject, options);
    }

    @Override
    protected SGG<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> calculateGraph(HLPetriGame hlgame) {
        return SGGBuilderHL.getInstance().create(new OneEnvHLPG(hlgame, true));
    }

}
