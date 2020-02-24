package uniolunisaar.adam.logic.pg.converter.hl;

import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

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
