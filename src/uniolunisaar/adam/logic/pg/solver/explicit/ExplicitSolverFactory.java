package uniolunisaar.adam.logic.pg.solver.explicit;

import uniolunisaar.adam.ds.objectives.Buchi;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.objectives.Reachability;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.logic.synthesis.solver.LLSolverFactory;

/**
 *
 * @author Manuel Gieseking
 */
public class ExplicitSolverFactory extends LLSolverFactory<ExplicitSolverOptions, AbstractExplicitSolver<? extends Condition<?>>> {

    private static ExplicitSolverFactory instance = null;

    public static ExplicitSolverFactory getInstance() {
        if (instance == null) {
            instance = new ExplicitSolverFactory();
        }
        return instance;
    }

    private ExplicitSolverFactory() {

    }

    @Override
    protected <W extends Condition<W>> ExplicitSolvingObject<W> createSolvingObject(PetriGameWithTransits game, W winCon) throws NotSupportedGameException {
        return new ExplicitSolvingObject<>(game, winCon);
    }

    @Override
    protected AbstractExplicitSolver<? extends Condition<?>> getESafetySolver(PetriGameWithTransits game, Safety con, ExplicitSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected ExplicitASafetyWithoutType2Solver getASafetySolver(PetriGameWithTransits game, Safety con, ExplicitSolverOptions options) throws SolvingException {
        return new ExplicitASafetyWithoutType2Solver(createSolvingObject(game, con), options);
    }

    @Override
    protected AbstractExplicitSolver<? extends Condition<?>> getEReachabilitySolver(PetriGameWithTransits game, Reachability con, ExplicitSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected AbstractExplicitSolver<? extends Condition<?>> getAReachabilitySolver(PetriGameWithTransits game, Reachability con, ExplicitSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected AbstractExplicitSolver<? extends Condition<?>> getEBuchiSolver(PetriGameWithTransits game, Buchi con, ExplicitSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    protected AbstractExplicitSolver<? extends Condition<?>> getABuchiSolver(PetriGameWithTransits game, Buchi con, ExplicitSolverOptions options) throws SolvingException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
