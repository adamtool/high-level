package uniolunisaar.adam.logic.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.AbstractSymbolicGameGraph;
import uniolunisaar.adam.ds.graph.hl.DecisionSet;
import uniolunisaar.adam.ds.graph.hl.IDecision;
import uniolunisaar.adam.ds.graph.hl.SGG;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLEnvDecision;
import uniolunisaar.adam.ds.graph.hl.SRGFlow;
import uniolunisaar.adam.ds.graph.hl.SGGByHashCode;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLCommitmentSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLSysDecision;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColorTokens;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.graph.hl.approachHL.IHLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.ValuationIterator;
import uniolunisaar.adam.ds.highlevel.Valuations;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.highlevel.symmetries.SymmetryIterator;

/**
 *
 * @author Manuel Gieseking
 */
public class SGGBuilderHL {

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
    public static SGGByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> createByHLGame(OneEnvHLPG hlgame) {
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
        SGGByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> srg = new SGGByHashCode<>(hlgame.getName() + "_HL_SGG", init);
        Stack<Integer> todo = new Stack<>();
        todo.push(init.getId());
        while (!todo.isEmpty()) { // as long as new states had been added            
            HLDecisionSet state = srg.getState(todo.pop());
            // if the current state contains tops, resolve them 
            if (!state.isMcut() && state.hasTop()) {
                Set<HLDecisionSet> succs = state.resolveTop();
                // add only the not to any existing state equivalent decision sets
                // otherwise only the flows are added to the belonging equivalent class
                SGGBuilder.addSuccessors(state, null, succs, syms, todo, srg);
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
                        SGGBuilder.addSuccessors(state, t, succs, syms, todo, srg);
                    } else {
//                        System.out.println("haven't");
                    }
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
    public static AbstractSymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> create(OneEnvHLPG hlgame) {
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
        AbstractSymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> srg = new SGG<>(hlgame.getName() + "_HL_SGG", init);
        Stack<HLDecisionSet> todo = new Stack<>();
        todo.push(init);
        while (!todo.isEmpty()) { // as long as new states had been added            
            HLDecisionSet state = todo.pop();
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
    static <P, T, DC extends IDecision<P, T>, S extends DecisionSet<P, T, DC>, F extends SRGFlow<T>>
            void addSuccessors(S pre, T t, Set<S> succs, Symmetries syms, Stack<S> todo, AbstractSymbolicGameGraph<P, T, DC, S, SRGFlow<T>> srg) {
        for (S succ : succs) {
            boolean newOne = true;
            int id = succ.getId();
            S copySucc = succ;
            for (SymmetryIterator iti = syms.iterator(); iti.hasNext();) {
                Symmetry sym = iti.next(); // todo: get rid of the identity symmetry, just do it in this case before looping
                copySucc = (S) succ.apply(sym);
                if (srg.contains(copySucc)) {
                    newOne = false;
                    break;
                }
            }

            if (newOne) {
                srg.addState(succ);
                todo.add(succ);
            } else {
                id = copySucc.getId();
            }
            srg.addFlow(new SRGFlow(pre.getId(), t, id));
        }
    }

}
