package uniolunisaar.adam.logic.pg.solver.explicit;

import uniolunisaar.adam.ds.solver.LLSolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class ExplicitSolverOptions extends LLSolverOptions {

    public ExplicitSolverOptions(boolean skipTests, boolean withAutomaticTransitAnnotation) {
        super("expl", skipTests, withAutomaticTransitAnnotation);
    }

    public ExplicitSolverOptions(boolean skipTests) {
        super(skipTests, "expl");
    }

    public ExplicitSolverOptions() {
        super("expl");
    }

}
