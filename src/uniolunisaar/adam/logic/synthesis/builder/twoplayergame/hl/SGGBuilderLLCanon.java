package uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphUsingIDs;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphUsingIDsBidiMap;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.StateIdentifier;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.CommitmentSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps.CanonDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps.LexiILLDecisionComparator;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps.OrderedCommitmentSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps.OrderedDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLEnvDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLSysDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.GameGraphBuilder;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 * I can just use the standard algorithms for building the graph due to all the
 * magic is implemented in resolving the top and the firing in the
 * OrderedDecisionSets (with which we start for the initial state).
 *
 * @author Manuel Gieseking
 */
public class SGGBuilderLLCanon extends GameGraphBuilder<HLPetriGame, Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, ? extends StateIdentifier>> {

    private static SGGBuilderLLCanon instance = null;

    public static SGGBuilderLLCanon getInstance() {
        if (instance == null) {
            instance = new SGGBuilderLLCanon();
        }
        return instance;
    }

    private SGGBuilderLLCanon() {
    }

    public enum SaveMapping {
        ALL,
        SOME,
        NONE
    }

    public enum Approach {
        ORDERED_DCS,
        ORDERED_BY_LIST,
        ORDERED_BY_TREE,
        APPROX
    }

    //todo: make it properly with locks, just a quick hack to not save it in
    // CanonDecisionSet and OrderedDecisionSet, because even this pointer is 
    // kind of expensive to copy for this large amount of states
    private Iterable<Symmetry> currentSymmetries;
    private PetriGameWithTransits currentLLGame;
    private HLPetriGame currentHLGame;

    // todo: just a hack to check if it's faster
    public HashMap<OrderedDecisionSet, OrderedDecisionSet> dcsOrdered2canon = new HashMap<>();
    public HashMap<Set<ILLDecision>, CanonDecisionSet> dcs2canon = new HashMap<>();

    public SaveMapping saveMapping = SaveMapping.ALL;
    public boolean withBidi = true;
    public Approach approach = Approach.ORDERED_BY_TREE;
    public boolean skipSomeSymmetries = true;

    public void clearBufferedData() {
        dcs2canon.clear();
        dcsOrdered2canon.clear();
    }

    public Iterable<Symmetry> getCurrentSymmetries() {
        return currentSymmetries;
    }

    public PetriGameWithTransits getCurrentLLGame() {
        return currentLLGame;
    }

    /**
     * It's the same as for SGGBuilderLL Todo: Do it properly ...
     *
     * @param pgame
     * @return
     */
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

    private OrderedDecisionSet createOrderedInitDecisionSet(HLPetriGame hlgame, PetriGameWithTransits pgame) {
        TreeSet<ILLDecision> inits = new TreeSet<>(new LexiILLDecisionComparator());
        boolean hasSysDecision = false;
        for (Place place : pgame.getPlaces()) {
            if (place.getInitialToken().getValue() > 0) {
                if (pgame.isEnvironment(place)) {
                    inits.add(new LLEnvDecision(pgame, place));
                } else {
                    hasSysDecision = true;
                    inits.add(new LLSysDecision(pgame, place, new OrderedCommitmentSet(pgame, true)));
                }
            }
        }
        OrderedDecisionSet dcs = new OrderedDecisionSet(inits, !hasSysDecision, false, pgame, hlgame.getSymmetries());// todo: attention bad is not calculated to save time
        if (saveMapping != SaveMapping.NONE) {
            dcsOrdered2canon.put(dcs, dcs);
        }
        return dcs;
    }

    /**
     * In contrast to the OrderedDecisionSet approach does this approach order
     * the decision set each time an order is needed.
     *
     * @param hlgame
     * @param pgame
     * @return
     */
    private LLDecisionSet createCanonInitDecisionSet(HLPetriGame hlgame, PetriGameWithTransits pgame) {
        Set<ILLDecision> inits = new HashSet<>();
        boolean hasSysDecision = false;
        for (Place place : pgame.getPlaces()) {
            if (place.getInitialToken().getValue() > 0) {
                if (pgame.isEnvironment(place)) {
                    inits.add(new LLEnvDecision(pgame, place));
                } else {
                    hasSysDecision = true;
                    inits.add(new LLSysDecision(pgame, place, new CommitmentSet(pgame, true)));
                }
            }
        }
//        CanonDecisionSet dcs = new CanonDecisionSet(inits, false, false, pgame, hlgame.getSymmetries());
        CanonDecisionSet dcs = new CanonDecisionSet(inits, !hasSysDecision, false, pgame);// todo: attention bad is not calculated to save time
        if (saveMapping != SaveMapping.NONE) {
            dcs2canon.put(inits, dcs);
        }
        return dcs;
    }

    /**
     * Exploits the symmetries of the given wellformed Petri net to create a
     * symbolic game graph.
     *
     * This version firstly creates the low-level version of the game and uses
     * canonical representatives.
     *
     * Here only the number of states are reduced. Not the reduction of the
     * edges is implemented.
     *
     * @param hlgame
     * @return
     */
//    public GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(HLPetriGame hlgame) {
//    public GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(HLPetriGame hlgame) {
    public AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> create(HLPetriGame hlgame) {
        // Convert the high-level game to its low-level version
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true);
        // set the current values
        currentHLGame = hlgame;
        currentLLGame = pgame;
        if (approach != Approach.APPROX) {
            currentSymmetries = hlgame.getSymmetries();
        }

        // calculate the system transitions
        Collection<Transition> sysTransitions = putSysAndSingleEnvTransitionsToExtention(pgame);
        // create initial decision set
        LLDecisionSet init;
        if (approach == Approach.ORDERED_DCS) {
            init = createOrderedInitDecisionSet(hlgame, pgame);
        } else {
            init = createCanonInitDecisionSet(hlgame, pgame);
        }

        // Create the graph iteratively
//        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg;
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> srg;
        if (!withBidi) {
            srg = new GameGraphUsingIDs<>(hlgame.getName() + "_SRG", init);
        } else {
            srg = new GameGraphUsingIDsBidiMap<>(hlgame.getName() + "_SRG", init);
        }
//        srg = new GameGraph<>(hlgame.getName() + "_SRG", init);
        addStatesIteratively(hlgame, srg, init, pgame.getTransitions(), sysTransitions);
//        System.out.println(dcs2canon.size());
        return srg;
    }

    @Override
    protected Collection<Transition> getTransitions(Collection<Transition> trans, HLPetriGame hlgame) {
        return trans;
    }
//    
//        @Override
//    protected <ID extends StateIdentifier> void addSuccessors(ILLDecision pre, Transition t, Set<ILLDecision> succs, Stack<ID> todo, AbstractGameGraph<Place, Transition, DecisionSet, S, ID, GameGraphFlow<T, ID>> srg) {
////        addSuccessors(pre, t, succs, syms, todo, srg);
//    }

    public HLPetriGame getCurrentHLGame() {
        return currentHLGame;
    }

}
