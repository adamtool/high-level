package uniolunisaar.adam.logic.synthesis.builder.pgwt.highlevel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.PNWTTools;
import uniolunisaar.adam.util.pgwt.TransitCalculator;

/**
 * Currently this is only a structural adapted version of
 * BDDPetriGameStrategyBuilder. Those two should be merged in the future.
 *
 * @author Manuel Gieseking
 */
public class PGStrategyBuilderLLorExplicit {

    static final String DELIM = "_";
    private static PGStrategyBuilderLLorExplicit instance = null;

    public static PGStrategyBuilderLLorExplicit getInstance() {
        if (instance == null) {
            instance = new PGStrategyBuilderLLorExplicit();
        }
        return instance;
    }

    PGStrategyBuilderLLorExplicit() {
    }

    public PetriGameWithTransits builtStrategy(PetriGameWithTransits game, GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> ggStrategy) {
        PetriGameWithTransits strategy = builtStrategy(game.getName(), ggStrategy);

        TransitCalculator.copyTokenflowsFromGameToStrategy(game, strategy);
        return strategy;
    }

    /**
     * Only meaningful for the explicit graph strategies.The symbolic variant
     * can be transformed into such an explicit version or a complete different
     * algorithm has to be implemented.
     *
     * @param name
     * @param ggStrategy
     * @return
     */
//    public <P, DC extends IDecision<P, Transition>, S extends IDecisionSet<P, Transition, DC, S>, ID extends StateIdentifier, F extends GameGraphFlow<Transition, ID>>
//            PetriGame builtStrategy(PetriGame game, GameGraph<P, Transition, DC, S, GameGraphFlow<Transition, S>> ggStrategy) {
    public PetriGameWithTransits builtStrategy(String name, GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> ggStrategy) {
        Logger.getInstance().addMessage("Calculate Petri game strategy.");
        PetriGameWithTransits strategy = new PetriGameWithTransits("Winning strategy of the system players of the net '" + name + "'.");
        DecisionSet init = ggStrategy.getInitial();
        // create the initial places
        List<Place> initial = new ArrayList<>(init.getMarking());

        // old: just done from the initial marking of the game
//        List<Place> initial = new ArrayList<>();
//        Marking initialMarking = game.getInitialMarking();
//        for (Place p : game.getPlaces()) {
//            if (initialMarking.getToken(p).getValue() > 0) {
//                Place place = strategy.createPlace(p.getId() + DELIM + init.getId());
//                strategy.setOrigID(place, p.getId());
//                place.copyExtensions(p);
//                place.setInitialToken(1);
//                initial.add(place);
//            }
//        }
        // calculate Petri game
        try { // TODO: debug!
            calculateStrategyByBFS(ggStrategy, strategy, init, initial);
        } catch (Exception e) {
            try {
                PNWTTools.savePnwt2PDF("error_petrinet", strategy, true);
                throw e;
            } catch (IOException ex) {
            }
        }

        Logger.getInstance().addMessage("Done calculating Petri game strategy.");
        return strategy;
    }

//    private <P, DC extends IDecision<P, Transition>, S extends IDecisionSet<P, Transition, DC, S>, ID extends StateIdentifier, F extends GameGraphFlow<Transition, ID>>
//            void calculateStrategyByBFS(GameGraph<P, Transition, DC, S, GameGraphFlow<Transition, S>> ggStrategy, PetriGame strategy, S initialState, List<Place> initialMarking) {
    private void calculateStrategyByBFS(GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> ggStrategy, PetriGameWithTransits strategy, DecisionSet initialState, List<Place> initialMarking) {

        Map<DecisionSet, List<Transition>> transitionMap = new HashMap<>();
        Map<DecisionSet, List<Transition>> stratTransitionMap = new HashMap<>();

        Map<Integer, List<Place>> visitedCuts = new HashMap<>();
        LinkedList<Pair<DecisionSet, List<Place>>> todoStates = new LinkedList<>();
        todoStates.add(new Pair<>(initialState, initialMarking));
        // add to visited cuts 
        visitedCuts.put(initialState.getId(), initialMarking);
        while (!todoStates.isEmpty()) {
//            if (Thread.interrupted()) {
//                CalculationInterruptedException e = new CalculationInterruptedException();
//                Logger.getInstance().addError(e.getMessage(), e);
//                throw e;
//            }
            Pair<DecisionSet, List<Place>> state = todoStates.poll();
            DecisionSet prevState = state.getFirst();
            List<Place> prevMarking = state.getSecond();
            for (GameGraphFlow<Transition, DecisionSet> flow : ggStrategy.getPostsetView(prevState)) {
                DecisionSet succState = ggStrategy.getState(flow.getTarget());
                List<Place> succMarking = new ArrayList<>(prevMarking);

                // Jump over tops (tau transitions)
                Transition t = flow.getTransition();
                if (t == null) { // prev was a top state
                    if (!visitedCuts.containsKey(succState.getId())) { // if not already visited
                        // there can only be one successor of a top state in a strategy (deterministic)
                        todoStates.add(new Pair<>(succState, succMarking));
                        // add to visited cuts 
                        visitedCuts.put(succState.getId(), succMarking);
                        break; // break is OK, since there is only one
                    } else {
                        // if we have an already visited cut, we have to find 
                        // the transition which let to the previous state (since this was a
                        // tau transition) to put the right postset to this transition
                        List<Transition> trans = transitionMap.get(prevState);
                        List<Transition> strats_t = stratTransitionMap.get(prevState);
                        for (int i = 0; i < trans.size(); i++) {
                            t = trans.get(i);
                            Transition strat_t = strats_t.get(i);
                            if (i == 0) { // only delete ones, since that had been all merged to those places
                                for (Place place : strat_t.getPostset()) { // delete the former wrongly created postset
                                    strategy.removePlace(place);
                                }
                            }
                            addBehaviorForVisitedSuccessors(strategy, visitedCuts, succState, t, strat_t, todoStates, prevMarking);
                        }
                    }
                } else { // prev was not a top state
                    // Create the new Transition
                    Transition strat_t = strategy.createTransition();
                    strat_t.setLabel(t.getId());

                    // Create the preset edges
                    for (Place p : t.getPreset()) { // all places in preset of the original Petri net
                        Place place = getSuitablePredecessor(strategy, p.getId(), prevMarking);
                        strategy.createFlow(place, strat_t);
                        succMarking.remove(place);
                    }

                    // Calculate the postset edges and possibly the new postset places
                    if (visitedCuts.containsKey(succState.getId())) { // already visited cut
                        addBehaviorForVisitedSuccessors(strategy, visitedCuts, succState, t, strat_t, todoStates, prevMarking);
                    } else {// new state reached
                        // create all postset places as new copies
                        for (Place p : t.getPostset()) {
                            Place strat_p = strategy.createPlace(p.getId() + DELIM + succState.getId());
                            strategy.setOrigID(strat_p, p.getId());
                            strat_p.copyExtensions(p);
                            strategy.createFlow(strat_t, strat_p);
                            succMarking.add(strat_p);
                        }
                        todoStates.add(new Pair<>(succState, succMarking));
                        // add to visited cuts 
                        visitedCuts.put(succState.getId(), succMarking);
                    }
                    // if the succ has a top, save the transition leeding to the state
                    // for creating the pg strategy
                    if (succState.hasTop()) {
                        // there could possibly be more than one predeccessor
                        List<Transition> strat_trans = (stratTransitionMap.containsKey(succState)) ? stratTransitionMap.get(succState) : new ArrayList<>();
                        strat_trans.add(strat_t);
                        List<Transition> trans = (transitionMap.containsKey(succState)) ? transitionMap.get(succState) : new ArrayList<>();
                        trans.add(t);
                        stratTransitionMap.put(succState, strat_trans);
                        transitionMap.put(succState, trans);
                    }
                }
            }
        }
    }

    <P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>> void addBehaviorForVisitedSuccessors(PetriGameWithTransits strategy, Map<Integer, List<Place>> visitedCuts, S succState, Transition t, Transition strat_t, LinkedList<Pair<S, List<Place>>> todoStates, List<Place> prevMarking) {
        // Don't create new places, only add the "suitable" flows and delete 
        // the places which had been created before, which are now double.
        List<Place> visitedMarking = visitedCuts.get(succState.getId());
        // Do not create new places. Take the belonging places and only add
        // the needed flows            
        for (Place p : t.getPostset()) {
            Place place = getSuitablePredecessor(strategy, p.getId(), visitedMarking);
            strategy.createFlow(strat_t, place);
        }
        // those places which haven't change through the transition but are in the marking
        // thus a new place could had been created before and must be deleted and mapped to an existing place
        for (Place place : visitedMarking) {
            if (!containsID(strategy, t.getPostset(), place)) {
                Place place1 = null;
                // find the possibly unnecessarly created place
                for (Place p : prevMarking) {
                    String placeOID = strategy.getOrigID(place);
                    String pOID = strategy.getOrigID(p);
                    if (!(!placeOID.equals(pOID) || place.equals(p))) {
                        // so there is a different incarnation of the same place in the already visited mcut and the current marking
                        place1 = p;
                        break;
                    }
                }
                if (place1 != null) {
                    // it should only be at most one transition, since we are in the unfolding and deterministic
                    Iterator<Transition> it = place1.getPreset().iterator();
                    Transition tr = it.next();
                    strategy.removePlace(place1);
                    strategy.createFlow(tr, place);
                    // update all markings
                    // first the one for the other successors:
                    prevMarking.remove(place1);
                    prevMarking.add(place);
                    // then all those which are still in the todo list. E.g. previous flows 
                    // with the same prevMarking calculated in the flow loop before
                    for (Pair<S, List<Place>> pair : todoStates) {
                        List<Place> mark = pair.getSecond();
                        if (mark.contains(place1)) {
                            mark.remove(place1);
                            mark.add(place);
                        }
                        // TODO: Test if necessare or call by by reference
                        //todoStates.push(new Pair<>(pair.getFirst(), mark));
                    }
                    // that's it since the visitedMarkings belong to mcuts and we would already had updated them
                }
            }
        }
    }

    private boolean containsID(PetriGameWithTransits game, Set<Place> places, Place place) {
        for (Place p : places) {
            if (p.getId().equals(game.getOrigID(place))) {
                return true;
            }
        }
        return false;
        // TODO: change to 1.8
        //return places.stream().anyMatch((p) -> (p.getId().equals(place.getExtension("origID"))));
    }

    /**
     * Searches in marking for a place with the original ID equal to placeid. In
     * marking the id should be put as extension to the place with the hashtag
     * origID.
     *
     * Where the function is used their should be only one, since otherwise this
     * would mean that two incarnation of the same place of the original net are
     * occupied in the unfolding.
     *
     * TODO: Attention here is a problem if we don't have a safe net! Then this
     * comparison could find more than one place.
     *
     * @param placeid - the ID of the place in the original Petri net
     * @param marking - the current marking of the unfolding
     * @return the place of the marking with the given placeid
     */
    Place getSuitablePredecessor(PetriGameWithTransits game, String placeid, List<Place> marking) {
        for (Place p : marking) {
            String id = game.getOrigID(p);
            if (id.equals(placeid)) {
                return p;
            }
        }
        //todo: error
        throw new RuntimeException("ERROR in PG-StratBuilder Predecessor! Place: " + placeid + " marking: " + marking.toString());
    }

}
