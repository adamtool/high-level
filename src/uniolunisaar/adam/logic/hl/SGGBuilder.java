package uniolunisaar.adam.logic.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLEnvDecision;
import uniolunisaar.adam.ds.graph.hl.SRGFlow;
import uniolunisaar.adam.ds.graph.hl.SymbolicGameGraph;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLCommitmentSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLSysDecision;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColorTokens;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.ValuationIterator;
import uniolunisaar.adam.ds.highlevel.Valuations;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.highlevel.symmetries.SymmetryIterator;
import uniolunisaar.adam.ds.graph.hl.approachHL.IHLDecision;
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
public class SGGBuilder {

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
    public static SymbolicGameGraph<LLDecisionSet, SRGFlow<Transition>> createByLLGame(HLPetriGame hlgame) {
        // Create the iterator for the symmetries
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        // Convert the high-level game to its low-level version
        PetriGame pgame = HL2PGConverter.convert(hlgame, true);
        // calculate the system transitions
        Collection<Transition> sysTransitions = new ArrayList<>();
        for (Transition transition : pgame.getTransitions()) {
            boolean isSystem = true;
            for (Place place : transition.getPreset()) {
                if (pgame.isEnvironment(place)) {
                    isSystem = false;
                }
            }
            if (isSystem) {
                sysTransitions.add(transition);
            }
        }
        pgame.putExtension("sysTransitions", sysTransitions);// todo: just a quick hack to not calculate them too often
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
        SymbolicGameGraph<LLDecisionSet, SRGFlow<Transition>> srg = new SymbolicGameGraph<>(hlgame.getName() + "_SRG", init);
        Stack<Integer> todo = new Stack<>();
        todo.push(init.getId());
        while (!todo.isEmpty()) { // as long as new states had been added            
            LLDecisionSet state = srg.getState(todo.pop());
            // if the current state contains tops, resolve them 
            if (!state.isMcut() && state.hasTop()) {
                Set<LLDecisionSet> succs = state.resolveTop();
                // add only the not to any existing state equivalent decision sets
                // otherwise only the flows are added to the belonging equivalent class
                addSuccessors(state, null, succs, syms, todo, srg);
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
                    addSuccessors(state, transition, succs, syms, todo, srg);
                } else {
//                        System.out.println("haven't");
                }
            }
        }
        return srg;
    }

    /**
     * Exploits the symmetries of the given wellformed Petri net to create a
     * symbolic game graph. This version directly works on the high-level Petri
     * game.
     *
     * Compare Huber's et al. algorithm
     *
     * @param hlgame
     * @return
     */
    public static SymbolicGameGraph<HLDecisionSet, SRGFlow<ColoredTransition>> createByHLGame(OneEnvHLPG hlgame) {
        // Create the iterator for the symmetries
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        // create initial decision set
        Set<IHLDecision> inits = new HashSet<>();
        for (Place place : hlgame.getPlaces()) {
            ColorTokens tokens = hlgame.getColorTokens(place);
            if (tokens == null) {
                continue;
            }
            if (hlgame.isEnvironment(place)) {
                for (ColorToken token : tokens) {
                    inits.add(new HLEnvDecision(place, token));
                }
            } else {
                for (ColorToken token : tokens) {
                    inits.add(new HLSysDecision(place, token, new HLCommitmentSet(true)));
                }
            }
        }
        HLDecisionSet init = new HLDecisionSet(inits, false, false, hlgame);

        // Create the graph iteratively
        SymbolicGameGraph<HLDecisionSet, SRGFlow<ColoredTransition>> srg = new SymbolicGameGraph<>(hlgame.getName() + "_SRG", init);
        Stack<Integer> todo = new Stack<>();
        todo.push(init.getId());
        while (!todo.isEmpty()) { // as long as new states had been added            
            HLDecisionSet state = srg.getState(todo.pop());
            // if the current state contains tops, resolve them 
            if (!state.isMcut() && state.hasTop()) {
                Set<HLDecisionSet> succs = state.resolveTop();
                // add only the not to any existing state equivalent decision sets
                // otherwise only the flows are added to the belonging equivalent class
                addSuccessors(state, null, succs, syms, todo, srg);
                continue;
            }

            // In mcuts only transitions having an env place in its preset are allowed to fire
            // whereas in the other states solely system transitions are valid
            Collection<Transition> transitions;
            if (state.isMcut()) {
                transitions = new ArrayList<>(hlgame.getTransitions());
                transitions.removeAll(hlgame.getSystemTransitions());
            } else {
                transitions = hlgame.getSystemTransitions();
            }
            for (Transition transition : transitions) {
                Valuations vals = hlgame.getValuations(transition);
                for (ValuationIterator it = vals.iterator(); it.hasNext();) {
                    Valuation val = it.next();
                    ColoredTransition t = new ColoredTransition(hlgame, transition, val);
                    Set<HLDecisionSet> succs = state.fire(t);
//                    if (!state.isMcut()) {
//                        System.out.println("liko to fire" + t);
//                    }
                    if (succs != null) { // had been firable
//                        if (!state.isMcut()) {
//                            System.out.println("can fire");
//                        }
                        // add only the not to any existing state equivalent decision sets
                        // otherwise only the flows are added to the belonging equivalent class
                        addSuccessors(state, t, succs, syms, todo, srg);
                    } else {
//                        System.out.println("haven't");
                    }
                }
            }
        }
        return srg;
    }

    /**
     * Adds a successor only if there is not already any equivalence class
     * (regarding the symmetries) containing the successor. The corresponding
     * flows are added anyways.
     *
     * @param succs
     * @param syms
     * @param todo
     * @param srg
     */
    private static void addSuccessors(HLDecisionSet pre, ColoredTransition t, Set<HLDecisionSet> succs, Symmetries syms, Stack<Integer> todo, SymbolicGameGraph<HLDecisionSet, SRGFlow<ColoredTransition>> srg) {
        for (HLDecisionSet succ : succs) {
            boolean newOne = true;
            // todo: could think of creating a copy of succ, to create really the succ state and not the one which is the last application of the
            // symmetry. Don't know if this leads to not checking so much symmetries during the search of existing ones?            
//            DecisionSet copySucc = new DecisionSet(succ); // did this thing now, so test which version is better. Could make the copy cheaper since we put new colors and variables anyways
            HLDecisionSet copySucc = succ;
            for (SymmetryIterator iti = syms.iterator(); iti.hasNext();) {
                Symmetry sym = iti.next(); // todo: get rid of the identity symmetry, just do it in this case before looping
                copySucc.apply(sym);
                if (srg.contains(copySucc)) {
                    newOne = false;
                    break;
                }
            }

            if (newOne) {
                srg.addState(copySucc);
                todo.add(copySucc.getId());
            } else {
                copySucc = succ; // replace it with the initial one, since we hope this is cheaper for finding the equivalent marking in the symmetry loop
            }
            srg.addFlow(new SRGFlow(pre.getId(), t, copySucc.getId()));
        }
    }

    /**
     * Adds a successor only if there is not already any equivalence class
     * (regarding the symmetries) containing the successor. The corresponding
     * flows are added anyways.
     *
     * @param succs
     * @param syms
     * @param todo
     * @param srg
     */
    private static void addSuccessors(LLDecisionSet pre, Transition t, Set<LLDecisionSet> succs, Symmetries syms, Stack<Integer> todo, SymbolicGameGraph<LLDecisionSet, SRGFlow<Transition>> srg) {
        for (LLDecisionSet succ : succs) {
            boolean newOne = true;
            // todo: could think of creating a copy of succ, to create really the succ state and not the one which is the last application of the
            // symmetry. Don't know if this leads to not checking so much symmetries during the search of existing ones?            
//            DecisionSet copySucc = new DecisionSet(succ); // did this thing now, so test which version is better. Could make the copy cheaper since we put new colors and variables anyways
            LLDecisionSet copySucc = succ;
            for (SymmetryIterator iti = syms.iterator(); iti.hasNext();) {
                Symmetry sym = iti.next(); // todo: get rid of the identity symmetry, just do it in this case before looping
                copySucc.apply(sym);
                if (srg.contains(copySucc)) {
                    newOne = false;
                    break;
                }
            }

            if (newOne) {
                srg.addState(copySucc);
                todo.add(copySucc.getId());
            } else {
                copySucc = succ; // replace it with the initial one, since we hope this is cheaper for finding the equivalent marking in the symmetry loop
            }
            srg.addFlow(new SRGFlow(pre.getId(), t, copySucc.getId()));
        }
    }
}
