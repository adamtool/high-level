package uniolunisaar.adam.logic.pg.converter.hl;

import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.IDecision;
import uniolunisaar.adam.ds.graph.IDecisionSet;

/**
 *
 * @author Manuel Gieseking
 */
public class SGStrat2Graphstrategy {

    /**
     * Here only high-level strategies can be used which have only reduced the
     * number of states by the symmetries and not additionally the number of
     * edges!
     *
     * @param <P>
     * @param <T>
     * @param <DC>
     * @param <S>
     * @param <F>
     * @param hlstrat
     * @return
     */
//    public <P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, ID extends StateIdentifier, F extends GameGraphFlow<T, ID>, DCout extends IDecision<Place, Transition>, Sout extends IDecisionSet<Place, Transition, DCout, Sout>, Fout extends GameGraphFlow<Transition, ID>>
//            GameGraph<Place, Transition, DCout, Sout, GameGraphFlow<Transition, Sout>> builtStrategy(PetriGame game, GameGraph<P, T, DC, S, GameGraphFlow<T, S>> strategy) {
    public static <P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>>
            GameGraph<P, T, DC, S, F> builtStrategy(GameGraph<P, T, DC, S, F> hlstrat) {
        S initHL = hlstrat.getInitial();
//        initHL.createLLDecisionSet(game);
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> strategy = new GameGraph<>("Low-Level strategy of " + hlstrat.getName(), initHL.createLLDecisionSet());
        return null;
    }

//    private static <P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>>
//            DecisionSet createState(S state) {
//
////        DecisionSet dcs = new DecisionSet(decisions, state.isMcut(), state.isBad(), game);
//
//    }
}
