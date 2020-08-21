package uniolunisaar.adam.logic.pg.solver.hl;

import java.io.IOException;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.graph.IDecision;
import uniolunisaar.adam.ds.graph.IDecisionSet;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.logic.pg.solver.SolverFactory;
import uniolunisaar.adam.ds.objectives.Buchi;
import uniolunisaar.adam.ds.objectives.Reachability;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <F>
 */
public abstract class AbstractHLSolverFactory<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, F extends GameGraphFlow<T, S>>
        extends SolverFactory<HLPetriGame, HLSolverOptions, HLSolver<? extends Condition<?>, P, T, DC, S, F>> {

    @Override
    public HLSolver<? extends Condition<?>, P, T, DC, S, F> getSolver(String path, HLSolverOptions options) throws IOException, ParseException, CouldNotFindSuitableConditionException, SolvingException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public HLSolver<? extends Condition<?>, P, T, DC, S, F> getSolver(HLPetriGame game, HLSolverOptions options) throws CouldNotFindSuitableConditionException, SolvingException {
        return super.getSolver(game, Condition.Objective.A_SAFETY, new HLSolverOptions()); // todo: currently there is just the A_Safety solver implemented.
    }

    @Override
    protected <W extends Condition<W>> HLSolvingObject<W> createSolvingObject(HLPetriGame game, W winCon) throws NotSupportedGameException {
        return new HLSolvingObject<>(game, winCon);
    }

    @Override
    protected HLSolver<Safety, P, T, DC, S, F> getESafetySolver(HLPetriGame game, Safety con, HLSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HLSolver<Reachability, P, T, DC, S, F> getEReachabilitySolver(HLPetriGame game, Reachability con, HLSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HLSolver<Reachability, P, T, DC, S, F> getAReachabilitySolver(HLPetriGame game, Reachability con, HLSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HLSolver<Buchi, P, T, DC, S, F> getEBuchiSolver(HLPetriGame game, Buchi con, HLSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected HLSolver<Buchi, P, T, DC, S, F> getABuchiSolver(HLPetriGame game, Buchi con, HLSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
