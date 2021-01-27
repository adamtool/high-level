package uniolunisaar.adam.tests.synthesis.hl;

import java.io.File;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.generators.highlevel.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.highlevel.PackageDeliveryHL;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.canonicalreps.BDDASafetyWithoutType2CanonRepHLSolver;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.canonicalreps.HLASafetyWithoutType2CanonRepSolverBDDApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.canonicalreps.HLSolverFactoryBDDApproachCanonReps;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.hlapproach.HLASafetyWithoutType2SolverHLApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.hlapproach.HLSolverFactoryHLApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.llapproach.HLASafetyWithoutType2SolverLLApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.llapproach.HLSolverFactoryLLApproach;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestCanonicalRepVsMembershipBDDApproach {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/sgg/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setVerbose(true);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        Logger.getInstance().addMessageStream("INTERMEDIATE_TIMING", System.out);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void firstTests() throws Exception {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
        BDDSolverOptions opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        HLASafetyWithoutType2CanonRepSolverBDDApproach solver = (HLASafetyWithoutType2CanonRepSolverBDDApproach) HLSolverFactoryBDDApproachCanonReps.getInstance().getSolver(hlgame, opt);

        BDDASafetyWithoutType2CanonRepHLSolver solver1 = solver.getSolver();
        solver1.initialize();
//
//        BDD init = solver1.getInitialDCSs();
//
//        System.out.println("%%%%%%% THE STATE ITSELF");
//        BDDTools.printDecodedDecisionSets(init, solver1, true);
////        BDDTools.saveStates2Pdf(outputDir + "testcm21_init", init, solver1);
////
//        BDD makeCanonical = solver1.makeCanonical(init);
//        BDD canonWell = makeCanonical.and(solver1.getWellformed(0));
//        System.out.println("%%%%%%% ALL");
//        BDDTools.printDecodedDecisionSets(canonWell, solver1, true);
//        

//        BDD transitions = solver1.getSystemTrans();
//        System.out.println("%%%%%%% THE SySTEM TRANSITIONS");
//        BDDTools.printDecodedDecisionSets(transitions, solver1, true);
//        BDDTools.saveStates2Pdf(outputDir + "testcm21_init", init, solver1);
//
////        BDDTools.saveStates2Pdf(outputDir + "testcm21", makeCanonical, solver1);
//
//        System.out.println("%%%%%%% Wellformed");
//        BDDTools.printDecodedDecisionSets(canonWell, solver1, true);
//        
//        BDDTools.saveStates2Pdf(outputDir + "testcm21_well", makeCanonical, solver1);
//        BDD makeCanonical = solver1.makeCanonical(solver1.badSysDCS(),0);
//        BDD canonWell = makeCanonical.and(solver1.getWellformed(0));
//        System.out.println("%%%%%%% ALL");
//        BDDTools.printDecodedDecisionSets(makeCanonical, solver1, true);
//        BDDTools.saveStates2Pdf(outputDir+"testcm21", makeCanonical, solver1);
//        
//        System.out.println("%%%%%%% Wellformed");
//        BDDTools.printDecodedDecisionSets(canonWell, solver1, true);
//        BDDTools.saveStates2Pdf(outputDir+"testcm21_well", makeCanonical, solver1);
//        create("CM21", hlgame);
    }

    @Test
    public void testCM() throws Exception {
        int a = 2;
        int b = 1;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(a, b, true);
        HLTools.saveHLPG2PDF(outputDir + "CM" + a + b, hlgame);
        PGTools.savePG2PDF(outputDir + "CM" + a + b + "_ll", HL2PGConverter.convert(hlgame), false);
        checkExistsStrat("CM" + a + "" + b, hlgame);
    }

    @Test
    public void testPD() throws Exception {
        HLPetriGame hlgame = PackageDeliveryHL.generateEwithPool(1, 4, true);
        checkExistsStrat("PD14", hlgame);
    }

    private void checkExistsStrat(String name, HLPetriGame hlgame) throws Exception {
        long timeHLApproach = 0;
        long timeCanonRepApproach = 0;
        long time, diff;

        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
//        time = System.currentTimeMillis();
//        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
//        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
//        diff = System.currentTimeMillis() - time;
//        timeHLApproach += diff;
//        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
//        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat", stratHL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
//        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
//        boolean llapproach = solverLL.existsWinningStrategy();
        diff = System.currentTimeMillis() - time;
//        timeLLApproach += diff;
//        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL strategy LL approach: " + llapproach + "" + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% CANON REPS
        time = System.currentTimeMillis();
        BDDSolverOptions opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        HLASafetyWithoutType2CanonRepSolverBDDApproach solver = (HLASafetyWithoutType2CanonRepSolverBDDApproach) HLSolverFactoryBDDApproachCanonReps.getInstance().getSolver(hlgame, opt);
        solver.getSolver().initialize();
        boolean canonrep = solver.existsWinningStrategy();
        diff = System.currentTimeMillis() - time;
        timeCanonRepApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL strategy CanonReps approach: " + canonrep + " " + Math.round((diff / 1000.0f) * 100.0) / 100.0);

        time = System.currentTimeMillis();
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true, true);
        DistrSysBDDSolver<? extends Condition<?>> solverExplBDD = DistrSysBDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(pgame, true, false), opt);
        Boolean llBDD = solverExplBDD.existsWinningStrategy();
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL strategy explicit BDD exists: " + llBDD + " " + Math.round((diff / 1000.0f) * 100.0) / 100.0);

    }

    private void checkGraphStrat(String name, HLPetriGame hlgame) throws Exception {
        long timeHLApproach = 0;
        long timeCanonRepApproach = 0;
        long time, diff;

        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
//        time = System.currentTimeMillis();
//        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
//        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
//        diff = System.currentTimeMillis() - time;
//        timeHLApproach += diff;
//        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
//        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat", stratHL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
//        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
//        timeLLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL graph strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat", stratLL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% CANON REPS
        time = System.currentTimeMillis();
        BDDSolverOptions opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        HLASafetyWithoutType2CanonRepSolverBDDApproach solver = (HLASafetyWithoutType2CanonRepSolverBDDApproach) HLSolverFactoryBDDApproachCanonReps.getInstance().getSolver(hlgame, opt);
        solver.getSolver().initialize();
        BDDGraph canonGGStrat = solver.calculateGraphStrategy();
//        BDDGraph canonGGStrat = solver.calculateGraphGame();
        diff = System.currentTimeMillis() - time;
        timeCanonRepApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL graph strategy CanonReps approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        BDDTools.saveGraph2PDF(outputDir + name + "Canon_Gstrat", canonGGStrat, solver.getSolver());
//        BDDTools.saveGraph2PDF(outputDir + name + "Canon_GraphGame", canonGGStrat, solver.getSolver());

        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true, true);
        DistrSysBDDSolver<? extends Condition<?>> solverExplBDD = DistrSysBDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(pgame, true, false), opt);
        BDDGraph graphBDD = solverExplBDD.getGraphStrategy();
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy explicit BDD " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        BDDTools.saveGraph2PDF(outputDir + name + "Expl_BDD_Gstrat_low", graphBDD, solverExplBDD);

    }

    private void create(String name, HLPetriGame hlgame) throws Exception {
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + name);
        HLTools.saveHLPG2PDF(outputDir + name, hlgame);

        long timeHLApproach = 0;
        long timeLLApproach = 0;
        long timeCanonRepApproach = 0;
        long timeExplApproach = 0;
        long timeExplBDDApproach = 0;
        long time, diff;
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HLGraphStrategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
//        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
//        GameGraphUsingIDs<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
        AbstractGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat", stratHL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
//        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeLLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL graph strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat", stratLL);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LLGraphStrategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
//        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
//        GameGraphUsingIDs<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
        AbstractGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat_low", stratHLlow);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
//        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeLLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat_low", stratLLlow);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true, true);
        // %%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        time = System.currentTimeMillis();
        ExplicitASafetyWithoutType2Solver solverExp = (ExplicitASafetyWithoutType2Solver) ExplicitSolverFactory.getInstance().getSolver(pgame, new ExplicitSolverOptions());
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
//        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy explicit " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "Expl_Gstrat_low", stratExpl);
        // %%%%%%%%%%%%%%%%%%%%%%%%% BDD
        time = System.currentTimeMillis();
        BDDSolverOptions opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        DistrSysBDDSolver<? extends Condition<?>> solverExplBDD = DistrSysBDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(pgame, true, false), opt);
        BDDGraph graphBDD = solverExplBDD.getGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplBDDApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy explicit BDD " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        BDDTools.saveGraph2PDF(outputDir + name + "Expl_BDD_Gstrat_low", graphBDD, solverExplBDD);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Petri game strategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        PetriGameWithTransits pgStratHL = solverHL.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% Petri game strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "HL_PGstrat", pgStratHL, true);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        PetriGameWithTransits pgStratLL = solverLL.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeLLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% Petri game strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "LL_PGstrat", pgStratLL, true);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        // %%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        time = System.currentTimeMillis();
        PetriGameWithTransits pgStratExpl = solverExp.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% Petri game strategy explicit " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "Expl_PGstrat", pgStratExpl, true);
        // %%%%%%%%%%%%%%%%%%%%%%%%% BDD
        time = System.currentTimeMillis();
        PetriGameWithTransits pgStratExplBDD = solverExplBDD.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplBDDApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% Petri game strategy explicit BDD " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "Expl_BDD_PGstrat", pgStratExplBDD, true);

        time = System.currentTimeMillis();
        boolean winning = solverHL.existsWinningStrategy();
        Assert.assertEquals(winning, solverLL.existsWinningStrategy());
        Assert.assertEquals(winning, solverExp.existsWinningStrategy());
        Assert.assertEquals(winning, solverExplBDD.existsWinningStrategy());
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("All existence took " + Math.round((diff / 1000.0f) * 100.0) / 100.0);

        Logger.getInstance().addMessage("HLApproach: " + Math.round((timeHLApproach / 1000.0f) * 100.0) / 100.0);
        Logger.getInstance().addMessage("LLApproach: " + Math.round((timeLLApproach / 1000.0f) * 100.0) / 100.0);
        Logger.getInstance().addMessage("ExplApproach: " + Math.round((timeExplApproach / 1000.0f) * 100.0) / 100.0);
        Logger.getInstance().addMessage("ExplBDDApproach: " + Math.round((timeExplBDDApproach / 1000.0f) * 100.0) / 100.0);
    }
}
