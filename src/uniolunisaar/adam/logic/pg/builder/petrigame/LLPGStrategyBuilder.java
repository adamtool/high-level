package uniolunisaar.adam.logic.pg.builder.petrigame;

import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.util.pg.TransitCalculator;

/**
 *
 * @author Manuel Gieseking
 */
public class LLPGStrategyBuilder extends AbstractPGStrategyBuilder<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> {

    private static LLPGStrategyBuilder instance = null;

    public static LLPGStrategyBuilder getInstance() {
        if (instance == null) {
            instance = new LLPGStrategyBuilder();
        }
        return instance;
    }

    public PetriGame builtStrategy(PetriGame game, GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> ggStrategy) {
        PetriGame strategy = builtStrategy(game.getName(), ggStrategy);

        TransitCalculator.copyTokenflowsFromGameToStrategy(game, strategy);
        return strategy;
    }

    @Override
    String getPlaceID(Place place) {
        return place.getId();
    }

    @Override
    String getTransitionID(Transition transition) {
        return transition.getId();
    }

    @Override
    Set<Place> getPostset(Transition transition) {
        return transition.getPostset();
    }

    @Override
    Set<Place> getPreset(Transition transition) {
        return transition.getPreset();
    }

    @Override
    void copyExtension(Place to, Place from) {
        to.copyExtensions(from);
    }

}
