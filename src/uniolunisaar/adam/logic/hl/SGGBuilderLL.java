package uniolunisaar.adam.logic.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.IntegerID;
import uniolunisaar.adam.ds.graph.hl.SGG;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.graph.hl.SGGByHashCode;
import uniolunisaar.adam.ds.graph.hl.StateIdentifier;
import uniolunisaar.adam.ds.graph.hl.approachLL.ILLDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLCommitmentSet;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLEnvDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLSysDecision;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class SGGBuilderLL extends SGGBuilder<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, ? extends StateIdentifier>> {

    private static SGGBuilderLL instance = null;

    public static SGGBuilderLL getInstance() {
        if (instance == null) {
            instance = new SGGBuilderLL();
        }
        return instance;
    }

    private SGGBuilderLL() {
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

    private LLDecisionSet createInitDecisionSet(PetriGame pgame) {
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
     */
    public SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> createByHashcode(HLPetriGame hlgame) {
        // Convert the high-level game to its low-level version
        PetriGame pgame = HL2PGConverter.convert(hlgame, true);
        // calculate the system transitions
        Collection<Transition> sysTransitions = putSysAndSingleEnvTransitionsToExtention(pgame);
        // create initial decision set
        LLDecisionSet init = createInitDecisionSet(pgame);

        // Create the graph iteratively
        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> srg = new SGGByHashCode<>(hlgame.getName() + "_SRG", init);
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
     * @param hlgame
     * @return
     */
    public SGG<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> create(HLPetriGame hlgame) {
        // Convert the high-level game to its low-level version
        PetriGame pgame = HL2PGConverter.convert(hlgame, true);
        // calculate the system transitions
        Collection<Transition> sysTransitions = putSysAndSingleEnvTransitionsToExtention(pgame);
        // create initial decision set
        LLDecisionSet init = createInitDecisionSet(pgame);

        // Create the graph iteratively
        SGG<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> srg = new SGG<>(hlgame.getName() + "_SRG", init);
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
    Collection<Transition> getTransitions(Collection<Transition> trans, HLPetriGame hlgame) {
        return trans;
    }

}
