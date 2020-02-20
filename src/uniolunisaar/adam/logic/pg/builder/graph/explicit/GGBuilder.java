package uniolunisaar.adam.logic.pg.builder.graph.explicit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.StateIdentifier;
import uniolunisaar.adam.ds.graph.explicit.CommitmentSet;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.explicit.EnvDecision;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.explicit.SysDecision;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.builder.graph.GameGraphBuilder;

/**
 *
 * @author Manuel Gieseking
 */
public class GGBuilder extends GameGraphBuilder<PetriGame, Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, ? extends StateIdentifier>> {

    private static GGBuilder instance = null;

    public static GGBuilder getInstance() {
        if (instance == null) {
            instance = new GGBuilder();
        }
        return instance;
    }

    private GGBuilder() {
    }

    private Collection<Transition> putSysAndSingleEnvTransitionsToExtention(PetriGame pgame) {
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

    private DecisionSet createInitDecisionSet(PetriGame pgame) {
        Set<ILLDecision> inits = new HashSet<>();
        for (Place place : pgame.getPlaces()) {
            if (place.getInitialToken().getValue() > 0) {
                if (pgame.isEnvironment(place)) {
                    inits.add(new EnvDecision(pgame, place));
                } else {
                    inits.add(new SysDecision(pgame, place, new CommitmentSet(pgame, true)));
                }
            }
        }
        return new DecisionSet(inits, false, false, pgame);
    }

    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(PetriGame pgame) {
        // calculate the system transitions
        Collection<Transition> sysTransitions = putSysAndSingleEnvTransitionsToExtention(pgame);
        // create initial decision set
        DecisionSet init = createInitDecisionSet(pgame);

        // Create the graph iteratively
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg = new GameGraph<>(pgame.getName() + "_SRG", init);
        addStatesIteratively(pgame, srg, init, pgame.getTransitions(), sysTransitions);
        return srg;
    }

    @Override
    protected Collection<Transition> getTransitions(Collection<Transition> trans, PetriGame pgame) {
        return trans;
    }

}
