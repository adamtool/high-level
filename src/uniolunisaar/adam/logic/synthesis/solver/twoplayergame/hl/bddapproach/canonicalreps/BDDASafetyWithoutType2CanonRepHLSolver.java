package uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.canonicalreps;

import java.util.List;
import java.util.Map;
import net.sf.javabdd.BDD;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.ds.synthesis.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.synthesis.highlevel.ColorDomain;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.StaticColorClass;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoStrategyExistentException;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.HLBDDSolvingObject;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.membership.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

/**
 * Solves symmetric Petri games with a safety objective with BDDs. This solver
 * can be used when there is no possibillity for system player to play
 * infinitely long without any further interaction with the environment.
 *
 * This approach uses the canonical representations to have unique successors.
 *
 * Not finished!
 *
 * @author Manuel Gieseking
 */
public class BDDASafetyWithoutType2CanonRepHLSolver extends BDDASafetyWithoutType2HLSolver {

    private final HLBDDSolvingObject<Safety> hlSolvingObject;

    public BDDASafetyWithoutType2CanonRepHLSolver(HLBDDSolvingObject<Safety> obj, Symmetries syms, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(obj.getObj(), syms, opts);
        hlSolvingObject = obj;
    }

    @Override
    protected BDD calcSystemTransitions() {
        BDD sysTrans = super.calcSystemTransitions();

        // this should be the problem of forall (A and B) vs forall A and forall B
//        BDD first = sysTrans.exist(getSecondBDDVariables());
//        // pre->post both are not canonical, make them canonical
//        BDD canonFirst = makeCanonical(first);
//        BDD second = sysTrans.exist(getFirstBDDVariables());
//        BDD canonSecond = shiftFirst2Second(makeCanonical(shiftSecond2First(second)));
//        return canonFirst.andWith(canonSecond);
        return sysTrans;
    }

    public BDD getSystemTrans() {
        return calcSystemTransitions();
    }

    @Override
    protected BDD calcEnvironmentTransitions() {
        BDD envTrans = super.calcEnvironmentTransitions();

        // this should be the problem of forall (A and B) vs forall A and forall B
        // pre->post both are not canonical, make them canonical        
//        BDD canonFirst = makeCanonical(envTrans);
//        BDD canonSecond = makeCanonical(shiftSecond2First(envTrans));
//        return canonFirst.andWith(canonSecond);
        return envTrans;
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    @Override
    protected BDD calcDCSs() throws CalculationInterruptedException {
        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&& I DO THE CALCULATION!");
        // if it is an mcut or not is already coded in the transitions itself            
        BDD trans = getBufferedEnvTransitions().or(getBufferedSystemTransitions());

        BDD init = getInitialDCSs();

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
            Q_ = Q.or(makeCanonical(succs));
        }
        return Q.andWith(getWellformed(0));
    }

    @Override
    protected BDD attractor(BDD F, boolean p1, BDD gameGraph, Map<Integer, BDD> distance) throws CalculationInterruptedException {
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
            Q_ = makeCanonical(pre).or(Q);
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

    @Override
    protected BDD initial() {
        return makeCanonical(super.initial());
    }

    /**
     *
     * @param bdd
     * @return
     */
    public BDD makeCanonical(BDD bdd) {
//        return bdd.andWith(canonicalRepresentatives());
        return shiftSecond2First(bdd.and(getSymmetries()).exist(getFirstBDDVariables())).andWith(canonicalRepresentatives());
    }

    /**
     * just not finished yet, but must be super expensive.
     *
     * @return
     */
    public BDD canonicalReps() {
        HLPetriGame hlgame = hlSolvingObject.getGame();
        // put the high-level places in a list to ensure a fixed order
        List<Place> hlplaces = null;

        // a hashmap of for every basiccolor class (or when it has subclasses for each static subclass)
        // mapped to an integer counter
        Map<String, Integer> colorClassCounter = null;

        // for each place p        
        BDD canon = getFactory().one();
        for (Place hlplace : hlplaces) {
            // get the color domain of the place
            ColorDomain colorDomain = hlgame.getColorDomain(hlplace);
            for (int i = 0; i < colorDomain.size(); i++) { // currently should be just one
                String basicColorClassId = colorDomain.get(i);
                BasicColorClass basicColorClass = hlgame.getBasicColorClass(basicColorClassId);
                if (basicColorClass.getStaticSubclasses().isEmpty()) {

                } else {
                    for (Map.Entry<String, StaticColorClass> subclassEntry : basicColorClass.getStaticSubclasses().entrySet()) { // for each static subclass
                        String subclassid = subclassEntry.getKey();
                        StaticColorClass subclass = subclassEntry.getValue();
                        Integer counter = colorClassCounter.get(subclassid);
                        //
                        subclass.getColors().get(counter + 2);
                    }
                }
                //  p.c_i' -> p.c_{i-1}'

            }
        }
        return null;
    }

    /**
     * new method
     *
     * @return
     */
    private BDD canonicalRepresentatives() {
        BDD symmetries = getSymmetries();
        BDD smaller = BDDTools.getSmallerBDD(getFactory());
//        BDD allSymmetric = shiftSecond2First(symmetries);
//        return allSymmetric.andWith((smaller.and(symmetries)).not()).exist(getSecondBDDVariables());
        BDD symAndSmaller = symmetries.andWith(smaller).exist(getSecondBDDVariables()).not();
        return symAndSmaller;
    }

}
