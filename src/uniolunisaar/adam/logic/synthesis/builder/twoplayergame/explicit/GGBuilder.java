package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.explicit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphUsingIDs;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.StateIdentifier;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.CommitmentSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.EnvDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.SysDecision;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.GameGraphBuilder;

/**
 *
 * @author Manuel Gieseking
 */
public class GGBuilder extends GameGraphBuilder<PetriGameWithTransits, Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, ? extends StateIdentifier>> {

    private static GGBuilder instance = null;

    public static GGBuilder getInstance() {
        if (instance == null) {
            instance = new GGBuilder();
        }
        return instance;
    }

    protected GGBuilder() {
    }

    public Collection<Transition> putSysAndSingleEnvTransitionsToExtention(PetriGameWithTransits pgame) {
        Collection<Transition> sysTransitions = new ArrayList<>();
        Collection<Transition> singlePresetTransitions = new ArrayList<>();
        for (Transition transition : pgame.getTransitions()) {
            boolean isSystem = true;
            for (Place place : transition.getPreset()) {
                if (pgame.isEnvironment(place)) {
                    isSystem = false;
                }
            }
            if (isSystem) {
                sysTransitions.add(transition);
                if (transition.getPreset().size() == 1) {
                    singlePresetTransitions.add(transition);
                }
            }
        }
        pgame.putExtension("sysTransitions", sysTransitions);// todo: just a quick hack to not calculate them too often
        pgame.putExtension("singlePresetTransitions", singlePresetTransitions);// todo: just a quick hack to not calculate them too often
        return sysTransitions;
    }

    public DecisionSet createInitDecisionSet(PetriGameWithTransits pgame) {
        Set<ILLDecision> inits = new HashSet<>();
        boolean hasSysDecision = false;
        for (Place place : pgame.getPlaces()) {
            if (place.getInitialToken().getValue() > 0) {
                if (pgame.isEnvironment(place)) {
                    inits.add(new EnvDecision(pgame, place));
                } else {
                    hasSysDecision = true;
                    inits.add(new SysDecision(pgame, place, new CommitmentSet(pgame, true)));
                }
            }
        }
        return new DecisionSet(inits, !hasSysDecision, false, pgame); // todo: attention bad is not calculated to save time
    }

//    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(PetriGameWithTransits pgame) {
    public GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(PetriGameWithTransits pgame) {
        // calculate the system transitions
        Collection<Transition> sysTransitions = putSysAndSingleEnvTransitionsToExtention(pgame);
        // create initial decision set
        DecisionSet init = createInitDecisionSet(pgame);

        // Create the graph iteratively
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg = new GameGraph<>(pgame.getName() + "_SRG", init);
        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg = new GameGraphUsingIDs<>(pgame.getName() + "_SRG", init);
        addStatesIteratively(pgame, srg, init, pgame.getTransitions(), sysTransitions);
        return srg;
    }

    @Override
    protected Collection<Transition> getTransitions(Collection<Transition> trans, PetriGameWithTransits pgame) {
        return trans;
    }

}
