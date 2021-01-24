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
import uniolunisaar.adam.util.benchmarks.synthesis.Benchmarks;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

/**
 * Solves symmetric Petri games with a safety objective with BDDs. This solver
 * can be used when there is no possibility for system player to play infinitely
 * long without any further interaction with the environment.
 *
 * This approach uses the canonical representations to have unique successors.
 *
 * Not finished! Has problems for the "make canonical" for all states in the BDD
 *
 * @author Manuel Gieseking
 */
public class BDDASafetyWithoutType2CanonRepHLSolver extends BDDASafetyWithoutType2HLSolver {

    private final HLBDDSolvingObject<Safety> hlSolvingObject;
    private BDD canonicalRepresentatives = null;

    public BDDASafetyWithoutType2CanonRepHLSolver(HLBDDSolvingObject<Safety> obj, Symmetries syms, BDDSolverOptions opts) throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        super(obj.getLLObj(), syms, opts);
        hlSolvingObject = obj;
    }

    @Override
    public void initialize() {
        super.initialize();
        getFactory().setNodeTableSize(10000000);
//        getFactory().setIncreaseFactor(2);
    }

//    @Override
//    protected BDD calcSystemTransitions() {
//        BDD sysTrans = super.calcSystemTransitions();
//
//        // this should be the problem of forall (A and B) vs forall A and forall B
////        BDD first = sysTrans.exist(getSecondBDDVariables());
////        // pre->post both are not canonical, make them canonical
////        BDD canonFirst = makeCanonical(first);
////        BDD second = sysTrans.exist(getFirstBDDVariables());
////        BDD canonSecond = shiftFirst2Second(makeCanonical(shiftSecond2First(second)));
////        return canonFirst.andWith(canonSecond);
//        return sysTrans;
//    }
//
//    public BDD getSystemTrans() {
//        return calcSystemTransitions();
//    }
//
//    @Override
//    protected BDD calcEnvironmentTransitions() {
//        BDD envTrans = super.calcEnvironmentTransitions();
//
//        // this should be the problem of forall (A and B) vs forall A and forall B
//        // pre->post both are not canonical, make them canonical        
////        BDD canonFirst = makeCanonical(envTrans);
////        BDD canonSecond = makeCanonical(shiftSecond2First(envTrans));
////        return canonFirst.andWith(canonSecond);
//        return envTrans;
//    }
// %%%%%%%%%%%%%%%%%%%%%%%%% The relevant ability of the solver %%%%%%%%%%%%%%%%
    @Override
    protected BDD calcDCSs() throws CalculationInterruptedException {
        Logger.getInstance().addMessage("Calculation of all decision sets ...", "INTERMEDIATE_TIMING");
        long time = System.currentTimeMillis();
        // if it is an mcut or not is already coded in the transitions itself            
        BDD trans = getBufferedEnvTransitions().or(getBufferedSystemTransitions());

//        BDD init = makeCanonical(getInitialDCSs());
        BDD init = getInitialDCSs();

        BDD Q = getZero();
        BDD Q_ = init;//.andWith(getWellformed(0));
        while (!Q_.equals(Q)) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            Q = Q_;

            BDD succs = getSuccs(trans.and(Q));
//            Q_ = Q.or(makeCanonical(succs));
            Q_ = Q.or(succs);
        }
        BDD ret = Q.andWith(getWellformed(0));
        Logger.getInstance().addMessage(".... finished calculation all decision sets (" + (System.currentTimeMillis() - time) / 1000.0f + ")", "INTERMEDIATE_TIMING");
        return ret;

    }

    @Override
    protected BDD attractor(BDD F, boolean p1, BDD gameGraph, Map<Integer, BDD> distance) throws CalculationInterruptedException {
        Logger.getInstance().addMessage("Calculation of attractor BDD ...", "INTERMEDIATE_TIMING");
        long time = System.currentTimeMillis();
        // Calculate the possibly restricted transitions to the given game graph
//        BDD graphSuccs = super.shiftFirst2Second(gameGraph);
//        BDD envTrans = getBufferedEnvTransitions().and(gameGraph).and(graphSuccs);
//        BDD sysTrans = getBufferedSystemTransitions().and(gameGraph).and(graphSuccs);
//BDD canon = getCanonicalRepresentatives().and(shiftFirst2Second(getCanonicalRepresentatives()));
        BDD canon = shiftFirst2Second(getCanonicalRepresentatives());
        BDD envTrans = getBufferedEnvTransitions().and(canon);
        BDD sysTrans = getBufferedSystemTransitions().and(canon);

        BDD Q = getZero();
        BDD Q_ = F;
        int i = 0;
        BDD lastAdded = Q_; // todo: check if this is expensive or cheaper
        while (!Q_.equals(Q)) {
            if (Thread.interrupted()) {
                CalculationInterruptedException e = new CalculationInterruptedException();
                Logger.getInstance().addError(e.getMessage(), e);
                throw e;
            }
            if (distance != null) {
                distance.put(i++, Q_);
            }
            System.out.println("%!!!!!!!!!!!!!!!!!!!! ROUND " + i++);
//            BDDTools.printDecodedDecisionSets(Q_, this, true);
            Q = Q_;
            BDD pre = p1 ? pre(Q, sysTrans, envTrans) : pre(Q, envTrans, sysTrans);
////            System.out.println(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%% CANON PRE AND NOT TOP");
////            BDDTools.printDecodedDecisionSets(makeCanonical(pre).and(getNotTop()), this, true);

            BDD canonPre = makeCanonical(pre);
            Q_ = canonPre.or(Q);

//            System.out.println(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%% CANON PRE AND NOT TOP");
//            BDDTools.printDecodedDecisionSets(canonPre.and(getNotTop()), this, true);
            System.out.println(" %%%%%%%%%%%%%%%%%%%%%%%%%%%%% CANON PRE AND NOT TOP");
            BDDTools.printDecodedDecisionSets(canonPre.and(Q.not()), this, true);
            lastAdded = canonPre.id();

        }
        BDD ret = Q_;//.andWith(getWellformed(0));
        Logger.getInstance().addMessage("... finished attractor (" + (System.currentTimeMillis() - time) / 1000.0f + ")", "INTERMEDIATE_TIMING");
        return ret;
    }

    @Override
    protected boolean exWinStrat() throws CalculationInterruptedException {
        if (!isInitialized()) {
            initialize();
        }
//        System.out.println(".......................succors");
//        Place p1 = getGame().getPlace("Sys_o0");
//        BDD test = codePlace(p1, 0, getGame().getPartition(p1));
//        Place p2 = getGame().getPlace("OK_m0");
//        test.andWith(codePlace(p2, 0, getGame().getPartition(p2)));
//        Place p3 = getGame().getPlace("ERR_m1");
//        test.andWith(codePlace(p3, 0, getGame().getPartition(p3)));
//        test.andWith(TOP[])

////        System.out.println("%%%%%%%%%%%%%%%%%%%%% INIT");
        BDD init = getInitialDCSs();
////        BDDTools.printDecodedDecisionSets(init, this, true);
////        System.out.println("%%%%%%%%%%%%%%%%%%%%% INIT CUT WELLFORMED(0)");
////        BDDTools.printDecodedDecisionSets(init.and(getWellformed(0)), this, true);
        BDD canonInit = makeCanonical(init);
        System.out.println("%%%%%%%%%%%%%%%%%%%%% CANON INIT");
        BDDTools.printDecodedDecisionSets(canonInit, this, true);
//        System.out.println("/////////////////// ENV POST CANON INIT");
//        BDD initEnvTrans = canonInit.and(getBufferedEnvTransitions());
//        BDDTools.printDecodedDecisionSets(initEnvTrans, this, true);
//////
//        System.out.println("/////////////////// INIT SUCCS");
//        BDD initSuccs = getSuccs(initEnvTrans);
//        BDDTools.printDecodedDecisionSets(initSuccs, this, true);
////
////        System.out.println("%%%%%%%%%%%%%%%%%%%%% INIT CUT SYMS");
////        BDD initSuccsSym = init.and(getSymmetries());
////        BDDTools.printDecodedDecisionSets(initSuccsSym, this, true);
////        System.out.println("%%%%%%%%%%%%%%%%%%%%% INIT CANON CUT SYMS");
////        BDD initCanonSym = canonInit.and(getSymmetries());
////        BDDTools.printDecodedDecisionSets(initCanonSym, this, true);
//
////        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMMETRIES!");
////        BDDTools.printDecodedDecisionSets(getSymmetries(), this, true);
////        BDDTools.printDecodedDecisionSets(getWellformed(0).and(getWellformed(1)), this, true);
////        
//////      
//        System.out.println("/////////////////// INIT SUCCS CANON");
//        BDD initSuccsCanon = makeCanonical(initSuccs);
//        BDDTools.printDecodedDecisionSets(initSuccsCanon, this, true);
//////      
//        BDD succsPre = shiftFirst2Second(initSuccsCanon).and(getBufferedEnvTransitions());
//        System.out.println("/////////////////// ENV POST CANON PRE");
//        BDDTools.printDecodedDecisionSets(succsPre, this, true);
////        System.out.println("%%%%%%%%%%%%%%%%%% WINNING");
////        BDDTools.printDecodedDecisionSets(getBufferedWinDCSs(), this, true);
//        BDD bad = badStates();
//        BDD badCanon = makeCanonical(bad).and(getBufferedDCSs());
//        System.out.println("%%%%%%%%%%%%%%%%%% BAD CANON");
//        BDDTools.printDecodedDecisionSets(badCanon, this, true);
////        System.out.println("%%%%%%%%%%%%%%%%%% BAD CANON CUT BUFFEREDDCS");
////        BDDTools.printDecodedDecisionSets(badCanon.and(getBufferedDCSs()), this, true);
////        System.out.println("//////////////////////////////////BAD PRE");
////        BDDTools.printDecodedDecisionSets(pre(bad, getBufferedSystemTransitions(), getBufferedEnvTransitions()), this, true);
////        System.out.println("//////////////////////////////////BAD CANON PRE");
////        BDDTools.printDecodedDecisionSets(pre(badCanon, getBufferedSystemTransitions(), getBufferedEnvTransitions()), this, true);
//
//        System.out.println("////////////////////////////////// BAD CANCON CUT ENVTRANS");
//        System.out.println("---------------------------------------------------------------------------------------- notTop");
//        BDD badCanonNotTop = badCanon.and(super.getNotTop());
//        BDDTools.printDecodedDecisionSets(badCanonNotTop, this, true);
//        
//        BDD badCanonShifted = shiftFirst2Second(badCanon);
//        System.out.println("---------------------------------------------------------------------------------------- shifted");
//        BDDTools.printDecodedDecisionSets(badCanonShifted, this, true);
//        System.out.println("---------------------------------------------------------------------------------------- and env");
//        BDDTools.printDecodedDecisionSets(calcEnvironmentTransitions().and(badCanonShifted), this, true);
//        System.out.println("%%%%%%%%%%%%%%%%%% WINNING CUT INI");
//        BDDTools.printDecodedDecisionSets(getBufferedWinDCSs().and(init), this, true);
//        BDD canon = getCanonicalRepresentatives();
//
//        System.out.println("%%%%%%%%%%%%%%%%%% WINNING CUT INI CUT CANON");
//        BDDTools.printDecodedDecisionSets(getBufferedWinDCSs().and(init).and(canon), this, true);
//        System.out.println("%%%%%%%%%%%%%%%%%% WINNING CUT INIt CUT BAD");
//        BDDTools.printDecodedDecisionSets(getBufferedWinDCSs().and(init).and(badStates()), this, true); /zero
        return !((getBufferedWinDCSs().and(makeCanonical(getInitialDCSs()))).isZero());
//        return true;
    }

    /**
     * Returns the winning decisionsets for the system players
     *
     * @return
     * @throws uniolunisaar.adam.exceptions.pnwt.CalculationInterruptedException
     */
    @Override
    protected BDD calcWinningDCSs(Map<Integer, BDD> distance) throws CalculationInterruptedException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().start(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Logger.getInstance().addMessage("Calculating fixpoint ...");
        // todo: it should be expensive to calculate the buffered dcss!? Why did I chose to use it? BECAUSE THIS SEEMS REALLY TO BE FASTER? And after the not() I need them anyhoe?
        // is it OK, to have makeCanonical around the getBufferedDCSs?
//        BDD fixedPoint = attractor(badStates(), true, makeCanonical(getBufferedDCSs()), distance).not().and(getBufferedDCSs());
//        BDD fixedPoint = attractor(makeCanonical(badStates()), true, getBufferedDCSs(), distance).not().and(getBufferedDCSs());
        BDD fixedPoint = attractor(makeCanonical(badStates()), true, getFactory().one(), distance).not().and(getBufferedDCSs());
//        BDD fixedPoint = attractor(badStates(), true, getFactory().one(), distance).not().and(getBufferedDCSs());
//        BDD fixedPoint = attractor(badStates(), true, getFactory().one(), distance).not();
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()), this, true);
//        BDDTools.printDecodedDecisionSets(fixedPoint.andWith(codePlace(getGame().getNet().getPlace("env1"), 0, 0)).andWith(getBufferedSystemTransition()).andWith(getNotTop()), this, true);
        Logger.getInstance().addMessage("... calculation of fixpoint done.");
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        Benchmarks.getInstance().stop(Benchmarks.Parts.FIXPOINT);
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TODO : FOR BENCHMARKS
        return fixedPoint;
    }

    @Override
    protected PetriGameWithTransits calculateStrategy() throws NoStrategyExistentException, CalculationInterruptedException {
        return super.calculateStrategy();
    }

// %%%%%%%%%%%%%%%%%%%%%%%%% END The relevant ability of the solver %%%%%%%%%%%%
    /**
     *
     * @param bdd
     * @return
     */
    public BDD makeCanonical(BDD bdd) throws CalculationInterruptedException {
        Logger.getInstance().addMessage("Calculation of make canonical ...", "INTERMEDIATE_TIMING");
        long time = System.currentTimeMillis();
//        return bdd.andWith(canonicalRepresentatives());
        BDD ret = shiftSecond2First(bdd.and(getSymmetries()).exist(getFirstBDDVariables())).and(getCanonicalRepresentatives());
        Logger.getInstance().addMessage(".... finished make canonical (" + (System.currentTimeMillis() - time) / 1000.0f + ")", "INTERMEDIATE_TIMING");
        return ret;
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
    private BDD canonicalRepresentatives() throws CalculationInterruptedException {
        Logger.getInstance().addMessage("Calculation of canonicalRepresentatives BDD ...", "INTERMEDIATE_TIMING");
        long time = System.currentTimeMillis();
        BDD symmetries = getSymmetries();
        BDD smaller = BDDTools.getSmallerBDD(getFactory());//.and(getWellformed(0).and(getWellformed(1)));
//        BDD allSymmetric = shiftSecond2First(symmetries);
//        return allSymmetric.andWith((smaller.and(symmetries)).not()).exist(getSecondBDDVariables());
        BDD notSymAndSmaller = symmetries.and(smaller).exist(getSecondBDDVariables()).not().and(getWellformed(0));
        Logger.getInstance().addMessage(".... finished calculation of canonicalRepresentatives BDD (" + (System.currentTimeMillis() - time) / 1000.0f + ")", "INTERMEDIATE_TIMING");
        return notSymAndSmaller;
    }

    private BDD getCanonicalRepresentatives() throws CalculationInterruptedException {
        if (canonicalRepresentatives == null) {
            canonicalRepresentatives = canonicalRepresentatives();
        }
        return canonicalRepresentatives;
    }

}
