package uniolunisaar.adam.logic.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.SRGFlow;
import uniolunisaar.adam.ds.graph.hl.SGGByHashCode;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
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
public class SGGBuilderLL {

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
    public static SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> createByLLGame(HLPetriGame hlgame) {
        // Create the iterator for the symmetries
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        // Convert the high-level game to its low-level version
        PetriGame pgame = HL2PGConverter.convert(hlgame, true);
        // calculate the system transitions
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
        // create initial decision set
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
        LLDecisionSet init = new LLDecisionSet(inits, false, false, pgame);

        // Create the graph iteratively
        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> srg = new SGGByHashCode<>(hlgame.getName() + "_SRG", init);
        Stack<Integer> todo = new Stack<>();
        todo.push(init.getId());
        while (!todo.isEmpty()) { // as long as new states had been added        
            LLDecisionSet state = srg.getState(todo.pop());
            // if the current state contains tops, resolve them 
            if (!state.isMcut() && state.hasTop()) {
                Set<LLDecisionSet> succs = state.resolveTop();
                // add only the not to any existing state equivalent decision sets
                // otherwise only the flows are added to the belonging equivalent class
                SGGBuilder.addSuccessors(state, null, succs, syms, todo, srg);
                continue;
            }

            // In mcuts only transitions having an env place in its preset are allowed to fire
            // whereas in the other states solely system transitions are valid
            Collection<Transition> transitions;
            if (state.isMcut()) {
                transitions = new ArrayList<>(pgame.getTransitions());
                transitions.removeAll(sysTransitions);
            } else {
                transitions = sysTransitions;
            }
            for (Transition transition : transitions) {
                Set<LLDecisionSet> succs = state.fire(transition);
//                    if (!state.isMcut()) {
//                        System.out.println("liko to fire" + t);
//                    }
                if (succs != null) { // had been firable
//                        if (!state.isMcut()) {
//                            System.out.println("can fire");
//                        }
                    // add only the not to any existing state equivalent decision sets
                    // otherwise only the flows are added to the belonging equivalent class
                    SGGBuilder.addSuccessors(state, transition, succs, syms, todo, srg);
                } else {
//                        System.out.println("haven't");
                }
            }
        }
        return srg;
    }

    private static boolean contains(Collection<LLDecisionSet> states, LLDecisionSet state) {
        for (LLDecisionSet state1 : states) {
//            if (state.equals(state1)) {
            if (state.hashCode() == state1.hashCode()) {
                return true;
            }
        }
        return false;
    }

}
