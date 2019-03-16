package uniolunisaar.adam.logic.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.CommitmentSet;
import uniolunisaar.adam.ds.graph.hl.DecisionSet;
import uniolunisaar.adam.ds.graph.hl.EnvDecision;
import uniolunisaar.adam.ds.graph.hl.IDecision;
import uniolunisaar.adam.ds.graph.hl.SRGFlow;
import uniolunisaar.adam.ds.graph.hl.SymbolicReachabilityGraph;
import uniolunisaar.adam.ds.graph.hl.SysDecision;
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

/**
 *
 * @author Manuel Gieseking
 */
public class SRGBuilder {

    /**
     * Compare Huber's et al. algorithm
     *
     * @param hlgame
     * @return
     */
    public static SymbolicReachabilityGraph<DecisionSet, SRGFlow> create(OneEnvHLPG hlgame) {
        // Create the iterator for the symmetries
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        // create initial decision set
        Set<IDecision> inits = new HashSet<>();
        for (Place place : hlgame.getPlaces()) {
            ColorTokens tokens = hlgame.getColorTokens(place);
            if (tokens == null) {
                continue;
            }
            if (hlgame.isEnvironment(place)) {
                for (ColorToken token : tokens) {
                    inits.add(new EnvDecision(place, token));
                }
            } else {
                for (ColorToken token : tokens) {
                    inits.add(new SysDecision(place, token, new CommitmentSet(true)));
                }
            }
        }
        DecisionSet init = new DecisionSet(inits, false, false, hlgame);

        // Create the graph iteratively
        SymbolicReachabilityGraph<DecisionSet, SRGFlow> srg = new SymbolicReachabilityGraph<>(hlgame.getName() + "_SRG", init);
        Stack<Integer> todo = new Stack<>();
        todo.push(init.getId());
        while (!todo.isEmpty()) { // as long as new states had been added            
            DecisionSet state = srg.getState(todo.pop());
            // if the current state contains tops, resolve them 
            if (!state.isMcut() && state.hasTop()) {
                Set<DecisionSet> succs = state.resolveTop();
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
                    Set<DecisionSet> succs = state.fire(t);
                    if(!state.isMcut()) {
                        System.out.println("liko to fire"+t);
                        }
                    if (succs != null) { // had been firable
                        if(!state.isMcut()) {
                        System.out.println("can fire");
                        }
                        // add only the not to any existing state equivalent decision sets
                        // otherwise only the flows are added to the belonging equivalent class
                        addSuccessors(state, t, succs, syms, todo, srg);
                    } else {
                        System.out.println("haven't");
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
    private static void addSuccessors(DecisionSet pre, ColoredTransition t, Set<DecisionSet> succs, Symmetries syms, Stack<Integer> todo, SymbolicReachabilityGraph<DecisionSet, SRGFlow> srg) {
        for (DecisionSet succ : succs) {
            boolean newOne = true;
            // todo: could think of creating a copy of succ, to create really the succ state and not the one which is the last application of the
            // symmetry. Don't know if this leads to not checking so much symmetries during the search of existing ones?            
            DecisionSet copySucc = new DecisionSet(succ); // did this thing now, so test which version is better. Could make the copy cheaper since we put new colors and variables anyways
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
