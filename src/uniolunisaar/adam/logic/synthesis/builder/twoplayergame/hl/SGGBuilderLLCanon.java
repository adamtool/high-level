package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.StateIdentifier;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps.LexiILLDecisionComparator;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps.OrderedCommitmentSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps.OrderedDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLEnvDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLSysDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.GameGraphBuilder;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 * I can just use the standard algorithms for building the graph due to all the
 * magic is implemented in resolving the top and the firing in the
 * OrderedDecisionSets (with which we start for the initial state).
 *
 * @author Manuel Gieseking
 */
public class SGGBuilderLLCanon extends GameGraphBuilder<HLPetriGame, Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, ? extends StateIdentifier>> {

    private static SGGBuilderLLCanon instance = null;

    public static SGGBuilderLLCanon getInstance() {
        if (instance == null) {
            instance = new SGGBuilderLLCanon();
        }
        return instance;
    }

    private SGGBuilderLLCanon() {
    }

    /**
     * It's the same as for SGGBuilderLL Todo: Do it properly ...
     *
     * @param pgame
     * @return
     */
    private Collection<Transition> putSysAndSingleEnvTransitionsToExtention(PetriGameWithTransits pgame) {
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

    private OrderedDecisionSet createInitDecisionSet(HLPetriGame hlgame, PetriGameWithTransits pgame) {
        TreeSet<ILLDecision> inits = new TreeSet<>(new LexiILLDecisionComparator());
        for (Place place : pgame.getPlaces()) {
            if (place.getInitialToken().getValue() > 0) {
                if (pgame.isEnvironment(place)) {
                    inits.add(new LLEnvDecision(pgame, place));
                } else {
                    inits.add(new LLSysDecision(pgame, place, new OrderedCommitmentSet(pgame, true)));
                }
            }
        }
        return new OrderedDecisionSet(inits, false, false, pgame, hlgame.getSymmetries());
    }

    /**
     * Exploits the symmetries of the given wellformed Petri net to create a
     * symbolic game graph.
     *
     * This version firstly creates the low-level version of the game and uses
     * canonical representatives.
     *
     * Here only the number of states are reduced. Not the reduction of the
     * edges is implemented.
     *
     * @param hlgame
     * @return
     */
    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(HLPetriGame hlgame) {
        // Convert the high-level game to its low-level version
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true);
        // calculate the system transitions
        Collection<Transition> sysTransitions = putSysAndSingleEnvTransitionsToExtention(pgame);
        // create initial decision set
        LLDecisionSet init = createInitDecisionSet(hlgame, pgame);

        // Create the graph iteratively
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg = new GameGraph<>(hlgame.getName() + "_SRG", init);
        addStatesIteratively(hlgame, srg, init, pgame.getTransitions(), sysTransitions);
        return srg;
    }

    @Override
    protected Collection<Transition> getTransitions(Collection<Transition> trans, HLPetriGame hlgame) {
        return trans;
    }

}
