package uniolunisaar.adam.logic.synthesis.builder.pgwt.highlevel;

import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class HLPGStrategyBuilder extends AbstractPGStrategyBuilder<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> {

    private static HLPGStrategyBuilder instance = null;

    public static HLPGStrategyBuilder getInstance() {
        if (instance == null) {
            instance = new HLPGStrategyBuilder();
        }
        return instance;
    }

    @Override
    String getPlaceID(ColoredPlace place) {
        return HL2PGConverter.getPlaceID(place.getPlace().getId(), place.getColor());
    }

    @Override
    String getTransitionID(ColoredTransition transition) {
        return HL2PGConverter.getTransition(transition.getTransition().getGraph(), transition.getTransition().getId(), transition.getVal()).getId();
    }

    @Override
    Set<ColoredPlace> getPostset(ColoredTransition transition) {
        return transition.getPostset();
    }

    @Override
    Set<ColoredPlace> getPreset(ColoredTransition transition) {
        return transition.getPreset();
    }

    @Override
    void copyExtension(Place to, ColoredPlace from) {
        to.copyExtensions(from.getPlace());
    }

}
