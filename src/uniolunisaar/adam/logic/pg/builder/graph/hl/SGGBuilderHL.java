package uniolunisaar.adam.logic.pg.builder.graph.hl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLEnvDecision;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.GameGraphByHashCode;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLSysDecision;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColorTokens;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.graph.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.ValuationIterator;
import uniolunisaar.adam.ds.highlevel.Valuations;
import uniolunisaar.adam.ds.graph.IntegerID;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.StateIdentifier;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLCommitmentSet;

/**
 *
 * @author Manuel Gieseking
 */
public class SGGBuilderHL extends SGGBuilder<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, ? extends StateIdentifier>> {

    private static SGGBuilderHL instance = null;

    public static SGGBuilderHL getInstance() {
        if (instance == null) {
            instance = new SGGBuilderHL();
        }
        return instance;
    }

    private SGGBuilderHL() {
    }

    private HLDecisionSet createInitDCS(OneEnvHLPG hlgame) {
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
        return new HLDecisionSet(inits, false, false, hlgame);
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
     *
     *
     * Think that this method had the problems with the identifying of different
     * nodes with the same hash value. But I'm not sure!
     *
     */
    @Deprecated
    public GameGraphByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, IntegerID>> createByHashcode(OneEnvHLPG hlgame) {
        // create initial decision set
        HLDecisionSet init = createInitDCS(hlgame);

        // Create the graph iteratively
        GameGraphByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, IntegerID>> srg = new GameGraphByHashCode<>(hlgame.getName() + "_HL_SGG", init);
        addStatesIteratively(hlgame, srg, init, hlgame.getTransitions(), hlgame.getSystemTransitions());
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
    public GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> create(OneEnvHLPG hlgame) {
        // create initial decision set
        HLDecisionSet init = createInitDCS(hlgame);

        // Create the graph iteratively
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> srg = new GameGraph<>(hlgame.getName() + "_HL_SGG", init);
        addStatesIteratively(hlgame, srg, init, hlgame.getTransitions(), hlgame.getSystemTransitions());
        return srg;
    }

    @Override
    protected Collection<ColoredTransition> getTransitions(Collection<Transition> trans, HLPetriGame hlgame) {
        Collection<ColoredTransition> transitions = new ArrayList<>();
        for (Transition transition : trans) {
            Valuations vals = hlgame.getValuations(transition);
            for (ValuationIterator it = vals.iterator(); it.hasNext();) {
                Valuation val = it.next();
                ColoredTransition t = new ColoredTransition(hlgame, transition, val);
                transitions.add(t);
            }
        }
        return transitions;
    }

}
