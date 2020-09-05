package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit;

import uniolunisaar.adam.ds.synthesis.solver.LLSolverOptions;

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
