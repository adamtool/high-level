package uniolunisaar.adam.logic.synthesis.transformers.highlevel;

import java.util.Map;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;

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
//    Transition applySymmmetry(Transition t, Symmetry sym, GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> hlstrat) {
//    Transition applySymmmetry(Transition t, Symmetry sym, GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> hlstrat) {
    Transition applySymmmetry(Transition t, Symmetry sym, AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> hlstrat) {
        PetriGameWithTransits pgame = hlstrat.getInitial().getGame();

        String hlID = HL2PGConverter.getOrigID(t);
        Valuation val = HL2PGConverter.getValuation(t);
        Valuation newVal = new Valuation();
        for (Map.Entry<Variable, Color> entry : val.entrySet()) {
            Variable var = entry.getKey();
            Color c = entry.getValue();
            newVal.put(var, sym.get(c));
        }
        return HL2PGConverter.getTransition(pgame, hlID, newVal);
    }

}
