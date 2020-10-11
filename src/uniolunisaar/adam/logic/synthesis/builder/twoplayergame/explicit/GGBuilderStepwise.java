package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.explicit;

/**
 *
 * @author Manuel Gieseking
 */
public class GGBuilderStepwise extends GGBuilder {

    private static GGBuilderStepwise instance = null;

    public static GGBuilderStepwise getInstance() {
        if (instance == null) {
            instance = new GGBuilderStepwise();
        }
        return instance;
    }

    private GGBuilderStepwise() {
    }

  
//    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(PetriGameWithTransits pgame) {
//        // calculate the system transitions
//        Collection<Transition> sysTransitions = putSysAndSingleEnvTransitionsToExtention(pgame);
//        // create initial decision set
//        DecisionSet init = createInitDecisionSet(pgame);
//
//        // Create the graph iteratively
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg = new GameGraph<>(pgame.getName() + "_SRG", init);
//        addStatesIteratively(pgame, srg, init, pgame.getTransitions(), sysTransitions);
//        return srg;
//    }

  
}
