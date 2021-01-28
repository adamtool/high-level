package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IntegerID;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphByHashCode;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphUsingIDsBidiMap;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.StateIdentifier;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLCommitmentSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLEnvDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLSysDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class SGGBuilderLL extends SGGBuilder<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, ? extends StateIdentifier>> {

    private static SGGBuilderLL instance = null;

    public static SGGBuilderLL getInstance() {
        if (instance == null) {
            instance = new SGGBuilderLL();
        }
        return instance;
    }

    private SGGBuilderLL() {
    }

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

    private LLDecisionSet createInitDecisionSet(PetriGameWithTransits pgame) {
        Set<ILLDecision> inits = new HashSet<>();
        for (Place place : pgame.getPlaces()) {
            if (place.getInitialToken().getValue() > 0) {
                if (pgame.isEnvironment(place)) {
                    inits.add(new LLEnvDecision(pgame, place));
                } else {
                    inits.add(new LLSysDecision(pgame, place, new LLCommitmentSet(pgame, true)));
                }
            }
        }
        return new LLDecisionSet(inits, false, false, pgame);
    }

    /**
     * Exploits the symmetries of the given wellformed Petri net to create a
     * symbolic game graph.
     *
     * This version firstly creates the low-level version of the game and just
     * exploits the color classes to find the admissable symmetries.
     *
     * Compare Huber's et al. algorithm
     *
     * @param hlgame
     * @return
     *
     *
     * Think that this method had the problems with the identifying of different
     * nodes with the same hash value. But I'm not sure!
     *
     */
    @Deprecated
    public GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> createByHashcode(HLPetriGame hlgame) {
        // Convert the high-level game to its low-level version
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true);
        // calculate the system transitions
        Collection<Transition> sysTransitions = putSysAndSingleEnvTransitionsToExtention(pgame);
        // create initial decision set
        LLDecisionSet init = createInitDecisionSet(pgame);

        // Create the graph iteratively
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> srg = new GameGraphByHashCode<>(hlgame.getName() + "_SRG", init);
        addStatesIteratively(hlgame, srg, init, pgame.getTransitions(), sysTransitions);
        return srg;
    }

    /**
     * Exploits the symmetries of the given wellformed Petri net to create a
     * symbolic game graph.
     *
     * This version firstly creates the low-level version of the game and just
     * exploits the color classes to find the admissable symmetries.
     *
     * Compare Huber's et al. algorithm
     *
     * Here only the number of states are reduced. Not the reduction of the
     * edges presented in the RvG paper is implemented.
     *
     * @param hlgame
     * @return
     */
    public AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(HLPetriGame hlgame) {
//    public GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(HLPetriGame hlgame) {
        // Convert the high-level game to its low-level version
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true);
        // calculate the system transitions
        Collection<Transition> sysTransitions = putSysAndSingleEnvTransitionsToExtention(pgame);
        // create initial decision set
        LLDecisionSet init = createInitDecisionSet(pgame);

        // Create the graph iteratively
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg = new GameGraph<>(hlgame.getName() + "_SRG", init);
//        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg = new GameGraphUsingIDs<>(hlgame.getName() + "_SRG", init);
        GameGraphUsingIDsBidiMap<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg = new GameGraphUsingIDsBidiMap<>(hlgame.getName() + "_SRG", init);
        addStatesIteratively(hlgame, srg, init, pgame.getTransitions(), sysTransitions);
        return srg;
    }

    private boolean contains(Collection<LLDecisionSet> states, LLDecisionSet state) {
        for (LLDecisionSet state1 : states) {
//            if (state.equals(state1)) {
            if (state.hashCode() == state1.hashCode()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected Collection<Transition> getTransitions(Collection<Transition> trans, HLPetriGame hlgame) {
        return trans;
    }

}
