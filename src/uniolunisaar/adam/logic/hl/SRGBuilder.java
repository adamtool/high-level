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
 * @author Manuel Gimport uniolunisaar.adam.ds.highlevel.symmetries.SymmetryIterator;
ieseking
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
        DecisionSet init = new DecisionSet(inits, false, hlgame);
        SymbolicReachabilityGraph<DecisionSet, SRGFlow> srg = new SymbolicReachabilityGraph<>(hlgame.getName() + "_SRG", init);
        Stack<Integer> todo = new Stack<>();
        todo.push(init.getId());
        while (!todo.isEmpty()) {
            DecisionSet state = srg.getState(todo.pop());
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
                    Set<DecisionSet> succs;
                    if (!state.isMcut() && state.hasTop()) {
                        succs = state.resolveTop();
                    } else {
                        succs = state.fire(t);
                    }
                    for (SymmetryIterator iti = syms.iterator(); iti.hasNext();) {
                        Symmetry sym = iti.next();
                        
                    }
                }

            }
        }

        return srg;
    }
}
