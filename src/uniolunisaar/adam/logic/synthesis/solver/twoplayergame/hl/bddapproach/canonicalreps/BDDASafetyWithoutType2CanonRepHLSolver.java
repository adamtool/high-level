package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.canonicalreps;

import java.util.Map;
import net.sf.javabdd.BDD;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.membership.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.tools.Logger;

/**
 * Solves symmetric Petri games with a safety objective with BDDs. This solver
 * can be used when there is no possibillity for system player to play
 * infinitely long without any further interaction with the environment.
 *
 * This approach uses the canonical representations to have unique successors.
 *
 * @author Manuel Gieseking
 */
public class BDDASafetyWithoutType2CanonRepHLSolver extends BDDASafetyWithoutType2HLSolver {

    public BDDASafetyWithoutType2CanonRepHLSolver(DistrSysBDDSolvingObject<Safety> obj, Symmetries syms, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(obj, syms, opts);
    }

    @Override
    protected BDD calcSystemTransitions() {
        BDD sysTrans = super.calcSystemTransitions();
        // pre->post both are not canonical, make them canonical
        return sysTrans;
    }

    @Override
    protected BDD calcEnvironmentTransitions() {
        BDD envTrans = super.calcEnvironmentTransitions();
        // pre->post both are not canonical, make them canonical
        return envTrans;
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    @Override
    protected BDD calcDCSs() throws CalculationInterruptedException {
        // if it is an mcut or not is already coded in the transitions itself            
        BDD trans = getBufferedEnvTransitions().or(getBufferedSystemTransitions());

        BDD init = makeCanonical(getInitialDCSs());

        BDD Q = getZero();
        BDD Q_ = init.andWith(getWellformed(0));
        while (!Q_.equals(Q)) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            Q = Q_;

            BDD succs = getSuccs(trans.and(Q));
            Q_ = Q.or(succs);
        }
        return Q.andWith(getWellformed(0));
    }

    @Override
    protected BDD attractor(BDD F, boolean p1, BDD gameGraph, Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // also all symmetric are bad
        gameGraph = getSuccs(gameGraph.and(getSymmetries()));
        // Calculate the possibly restricted transitions to the given game graph
        BDD graphSuccs = super.shiftFirst2Second(gameGraph);
        BDD envTrans = getBufferedEnvTransitions().and(gameGraph).and(graphSuccs);
        BDD sysTrans = getBufferedSystemTransitions().and(gameGraph).and(graphSuccs);

        BDD Q = getZero();
        BDD Q_ = F;
        int i = 0;
        while (!Q_.equals(Q)) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            if (distance != null) {
                distance.put(i++, Q_);
            }
            Q = Q_;
            BDD pre = p1 ? pre(Q, sysTrans, envTrans) : pre(Q, envTrans, sysTrans);
            pre = getSuccs(pre.and(getSymmetries())).and(getBufferedDCSs());
            Q_ = pre.or(Q);
        }
        return Q_.andWith(getWellformed(0));
    }

    @Override
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return super.calculateStrategy();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% END The relevant ability of the solver %%%%%%%%%%%%
    @Override
    protected BDD calcBadDCSs() {
        return makeCanonical(badStates());
    }

    private BDD makeCanonical(BDD bdd) {

        return bdd;
    }
}
