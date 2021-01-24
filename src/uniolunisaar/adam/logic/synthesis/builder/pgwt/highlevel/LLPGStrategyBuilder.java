package uniolunisaar.adam.logic.synthesis.builder.pgwt.highlevel;

import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphUsingIDs;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.util.pgwt.TransitCalculator;

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

//    public PetriGameWithTransits builtStrategy(PetriGameWithTransits game, GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> ggStrategy) {
    public PetriGameWithTransits builtStrategy(PetriGameWithTransits game, GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> ggStrategy) {
        PetriGameWithTransits strategy = builtStrategy(game.getName(), ggStrategy);

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
