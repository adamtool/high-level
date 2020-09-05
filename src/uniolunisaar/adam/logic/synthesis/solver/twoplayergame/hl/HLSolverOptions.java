package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl;

import uniolunisaar.adam.ds.synthesis.solver.SolverOptions;

/**
 *
 * @author Manuel Gieseking
 */
public class HLSolverOptions extends SolverOptions {

    public enum Approach {
        HL,
        LL,
        BDD
    }

    private Approach approach = Approach.LL;

    public HLSolverOptions() {
        super("hl");
    }

    public HLSolverOptions(boolean skipTests) {
        super("hl", skipTests);
    }

    public Approach getApproach() {
        return approach;
    }

    public void setApproach(Approach approach) {
        this.approach = approach;
    }

}
