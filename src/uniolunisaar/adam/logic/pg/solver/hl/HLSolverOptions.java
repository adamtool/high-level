package uniolunisaar.adam.logic.pg.solver.hl;

import uniolunisaar.adam.ds.solver.SolverOptions;

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

    public HLSolverOptions(String name) {
        super(name);
    }

    public Approach getApproach() {
        return approach;
    }

    public void setApproach(Approach approach) {
        this.approach = approach;
    }

}
