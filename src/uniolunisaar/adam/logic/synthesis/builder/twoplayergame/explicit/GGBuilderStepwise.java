package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.explicit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphUsingIDs;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDState;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.util.ExplicitBDDGraphTransformer;

/**
 *
 * @author Manuel Gieseking
 */
public class GGBuilderStepwise {

//    private final GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> sgg;
    private final GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> sgg;
    private final Collection<Transition> systemTransitions;

    // just for the webinterface to not newly implement the frontend.
    Map<DecisionSet, Integer> bddIDs = new HashMap<>();
    Map<BDDState, DecisionSet> dcsMapping = new HashMap<>();

    public GGBuilderStepwise(PetriGameWithTransits pgame) {
        // create initial decision set
        DecisionSet init = GGBuilder.getInstance().createInitDecisionSet(pgame);
        systemTransitions = GGBuilder.getInstance().putSysAndSingleEnvTransitionsToExtention(pgame);
        // Create the graph with the init place
//        sgg = new GameGraph<>(pgame.getName() + "_SRG", init);
        sgg = new GameGraphUsingIDs<>(pgame.getName() + "_SRG", init);
    }

    /**
     * This is just for the webinterface to not newly implement the frontend.
     *
     * @param game
     * @param bddGraph
     */
    public GGBuilderStepwise(PetriGameWithTransits game, BDDGraph bddGraph) {
        this(game);
        BDDState init = bddGraph.addState(ExplicitBDDGraphTransformer.decisionset2BDDState(sgg.getInitial()));
        bddGraph.setInitial(init);
        bddIDs.put(sgg.getInitial(), init.getId());
        dcsMapping.put(init, sgg.getInitial());
    }

    /**
     * This is just for the webinterface to not newly implement the frontend.
     *
     * @param state
     * @param game
     * @param bddGraph
     */
    public void addSuccessors(BDDState state, PetriGameWithTransits game, BDDGraph bddGraph) {
        DecisionSet dcsState = dcsMapping.get(state);
        Pair<List<GameGraphFlow<Transition, DecisionSet>>, List<DecisionSet>> added = addSuccessors(dcsState, game);
        // create the new states also in the bddgraph
        for (DecisionSet dcs : added.getSecond()) {
            DecisionSet succState = sgg.getState(dcs);
            BDDState bddState = bddGraph.addState(ExplicitBDDGraphTransformer.decisionset2BDDState(succState));
            bddIDs.put(succState, bddState.getId());
            dcsMapping.put(bddState, succState);
            if (dcs.isBad()) {
                bddState.setBad(true);
            }
            if (dcs.isMcut()) {
                bddState.setEnvState(true);
            }
        }
        // create the flows
        for (GameGraphFlow<Transition, DecisionSet> flow : added.getFirst()) {
            DecisionSet preDcs = sgg.getState(flow.getSource());
            DecisionSet postDcs = sgg.getState(flow.getTarget());
            BDDState pre = bddGraph.getState(bddIDs.get(preDcs));
            BDDState post = bddGraph.getState(bddIDs.get(postDcs));
            bddGraph.addFlow(pre, post, flow.getTransition());
        }
    }

    public Pair<List<GameGraphFlow<Transition, DecisionSet>>, List<DecisionSet>> addSuccessors(DecisionSet state, PetriGameWithTransits game) {
        List<GameGraphFlow<Transition, DecisionSet>> flows = new ArrayList<>();
        List<DecisionSet> states = new ArrayList<>();
        Map<Transition, Set<DecisionSet>> succs = GGBuilder.getInstance().getSuccessors(state, game.getTransitions(), systemTransitions, game);
        for (Transition t : succs.keySet()) {
            for (DecisionSet succ : succs.get(t)) {
                DecisionSet id = sgg.getID(succ);
                if (!sgg.contains(succ)) {
                    sgg.addState(succ);
                    states.add(succ);
                } else {
                    id = sgg.getID(succ);
                }
                GameGraphFlow<Transition, DecisionSet> flow = new GameGraphFlow<>(sgg.getID(state), t, id);
                sgg.addFlow(flow);
                flows.add(flow);
            }
        }
        return new Pair<>(flows, states);
    }
}
