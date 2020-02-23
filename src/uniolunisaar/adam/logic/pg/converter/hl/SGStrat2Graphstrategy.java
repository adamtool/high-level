package uniolunisaar.adam.logic.pg.converter.hl;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.IDecision;
import uniolunisaar.adam.ds.graph.IDecisionSet;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;

/**
 *
 * @author Manuel Gieseking
 */
public class SGStrat2Graphstrategy {

//    public <P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, ID extends StateIdentifier, F extends GameGraphFlow<T, ID>, DCout extends IDecision<Place, Transition>, Sout extends IDecisionSet<Place, Transition, DCout, Sout>, Fout extends GameGraphFlow<Transition, ID>>
//            GameGraph<Place, Transition, DCout, Sout, GameGraphFlow<Transition, Sout>> builtStrategy(PetriGame game, GameGraph<P, T, DC, S, GameGraphFlow<T, S>> strategy) {
    public static <P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>>
            GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> builtStrategy(GameGraph<P, T, DC, S, F> hlstrat) {
                
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> strategy = new GameGraph<>("Low-Level strategy of " + hlstrat.getName(), init);
        return null;
    }

}
