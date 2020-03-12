package uniolunisaar.adam.logic.pg.converter.hl;

import java.util.Map;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.petrigame.PetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public class LLSGStrat2Graphstrategy extends AbstractSGStrat2Graphstrategy<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> {

    private static LLSGStrat2Graphstrategy instance = null;

    public static LLSGStrat2Graphstrategy getInstance() {
        if (instance == null) {
            instance = new LLSGStrat2Graphstrategy();
        }
        return instance;
    }

    @Override
    GameGraphFlow<Transition, DecisionSet> createFlow(DecisionSet pre, Transition transition, DecisionSet succ) {
        return new GameGraphFlow<>(pre, transition, succ);
    }

    @Override
    Transition applySymmmetry(Transition t, Symmetry sym, GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> hlstrat) {
        PetriGame pgame = hlstrat.getInitial().getGame();

        String hlID = HL2PGConverter.getOrigID(t);
        Valuation val = HL2PGConverter.getValuation(t);
        Valuation newVal = new Valuation();
        for (Map.Entry<Variable, Color> entry : val.entrySet()) {
            Variable var = entry.getKey();
            Color c = entry.getValue();
            newVal.put(var, sym.get(c));
        }
        return pgame.getTransition(HL2PGConverter.getTransitionID(hlID, newVal));
    }

}