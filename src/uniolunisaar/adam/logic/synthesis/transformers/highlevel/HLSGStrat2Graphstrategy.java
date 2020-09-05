package uniolunisaar.adam.logic.synthesis.transformers.highlevel;

import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 */
public class HLSGStrat2Graphstrategy extends AbstractSGStrat2Graphstrategy<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> {

    private static HLSGStrat2Graphstrategy instance = null;

    public static HLSGStrat2Graphstrategy getInstance() {
        if (instance == null) {
            instance = new HLSGStrat2Graphstrategy();
        }
        return instance;
    }

    @Override
    GameGraphFlow<ColoredTransition, HLDecisionSet> createFlow(HLDecisionSet pre, ColoredTransition transition, HLDecisionSet succ) {
        return new GameGraphFlow<>(pre, transition, succ);
    }

    @Override
    ColoredTransition applySymmmetry(ColoredTransition t, Symmetry sym, GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> hlstrat) {
        return t.apply(sym);
    }

}
