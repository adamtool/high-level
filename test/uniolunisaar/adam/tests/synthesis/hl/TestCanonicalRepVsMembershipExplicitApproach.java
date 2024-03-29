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
import uniolunisaar.adam.generators.highlevel.ClientServerHL;
import uniolunisaar.adam.generators.highlevel.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.highlevel.DocumentWorkflowHL;
import uniolunisaar.adam.generators.highlevel.PackageDeliveryHL;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderLLCanon;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.mcutscheduling.safe.DistrSysBDDSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.canonicalreps.HLASafetyWithoutType2CanonRepSolverBDDApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.canonicalreps.HLSolverFactoryBDDApproachCanonReps;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.canonicalreps.HLASafetyWithoutType2SolverCanonApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.canonicalreps.HLSolverFactoryCanonApproach;
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
public class TestCanonicalRepVsMembershipExplicitApproach {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/sgg/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setVerbose(true);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
//        Logger.getInstance().addMessageStream("INTERMEDIATE_TIMING", System.out);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void firstTests() throws Exception {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(4, 2, true);

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
    public void testCS() throws Exception {
        int a = 3;
        HLPetriGame hlgame = ClientServerHL.create(a, true);
//        HLTools.saveHLPG2PDF(outputDir + "CS" + a, hlgame);
//        PGTools.savePG2PDF(outputDir + "CS" + a + "_ll", HL2PGConverter.convert(hlgame), false);
        checkExistsStrat("CS" + a, hlgame);
    }

    @Test
    public void testCM() throws Exception {
        int a = 2;
        int b = 4;
//        int a = 4;
//        int b = 2;
//        int a = 2;
//        int b = 1;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(a, b, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM" + a + b, hlgame);
//        PGTools.savePG2PDF(outputDir + "CM" + a + b + "_ll", HL2PGConverter.convert(hlgame), false);
        checkExistsStrat("CM" + a + "" + b, hlgame);
    }
    @Test
    public void testDWs() throws Exception {
        int a = 4;
//        int a = 4;
//        int b = 2;
//        int a = 2;
//        int b = 1;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(a, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM" + a + b, hlgame);
//        PGTools.savePG2PDF(outputDir + "CM" + a + b + "_ll", HL2PGConverter.convert(hlgame), false);
        checkExistsStrat("DWs" + a, hlgame);
    }

    @Test
    public void testPD() throws Exception {
        int a = 1;
        int b = 2;
        HLPetriGame hlgame = PackageDeliveryHL.generateEwithPool(a, b, true);
//        HLTools.saveHLPG2PDF(outputDir + "PD" + a + b, hlgame);
//        PGTools.savePG2PDF(outputDir + "PD" + a + b + "_ll", HL2PGConverter.convert(hlgame), false);
        checkExistsStrat("PD" + a + "" + b, hlgame);
    }
    private final int ROUNDS = 1;

    private void checkExistsStrat(String name, HLPetriGame hlgame2) throws Exception {
        Logger.getInstance().addMessage(name, false, true);
        long timeHLApproach = 0;
        long timeCanonRepApproach = 0;
        long time, diff;
        double mean;

        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
//        time = System.currentTimeMillis();
//        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
//        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
//        diff = System.currentTimeMillis() - time;
//        timeHLApproach += diff;
//        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
//        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat", stratHL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        mean = 0;
        for (int i = 0; i < ROUNDS; i++) {
            time = System.currentTimeMillis();
            HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(4, true);
            HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
            boolean llapproach = solverLL.existsWinningStrategy();
            int sizeLL = solverLL.getGraph().getStatesView().size();
            int sizeLLFlows = solverLL.getGraph().getFlows().size();
//        int sizeLL = -1;
            diff = System.currentTimeMillis() - time;
//        timeLLApproach += diff;
            double runningTime = Math.round((diff / 1000.0f) * 100.0) / 100.0;
            mean += runningTime;
            Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL strategy LL approach (size " + sizeLL + "/" + sizeLLFlows + "): " + llapproach + " " + runningTime, false, true);
            System.out.println(solverLL.getGraph().getBadStatesView().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "hl2llGG", solverLL.getGraph());
//        System.out.println("strat size" + solverLL.calculateGraphStrategy().getStatesView().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "hl2LLGGStrat", solverLL.calculateGraphStrategy());
//        HLTools.saveGraph2PDF(outputDir + "hl2LLGGLLStrat", solverLL.calculateLLGraphStrategy());
//        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true);
//        PGTools.savePG2PDF(outputDir + "hl2llPGStrat", solverLL.getStrategy(), false);
            // s.th. like all what is saved in hlgame (like symmetries, and the stuff for the converter) should be cleared
        }
        Logger.getInstance().addMessage("Mean: " + mean / ROUNDS, false, true);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% CANON REPS   
//        SGGBuilderLLCanon.getInstance().saveMapping = SGGBuilderLLCanon.SaveMapping.NONE;
        SGGBuilderLLCanon.getInstance().saveMapping = SGGBuilderLLCanon.SaveMapping.SOME;
//            SGGBuilderLLCanon.getInstance().approach = SGGBuilderLLCanon.Approach.ORDERED_DCS;
//        SGGBuilderLLCanon.getInstance().approach = SGGBuilderLLCanon.Approach.ORDERED_BY_TREE;
        SGGBuilderLLCanon.getInstance().approach = SGGBuilderLLCanon.Approach.ORDERED_BY_LIST;
//        SGGBuilderLLCanon.getInstance().approach = SGGBuilderLLCanon.Approach.APPROX;
        SGGBuilderLLCanon.getInstance().skipSomeSymmetries = true;
        mean = 0;
        for (int i = 0; i < ROUNDS; i++) {
            time = System.currentTimeMillis();

            HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(4, true);
            HLASafetyWithoutType2SolverCanonApproach solverCanon = (HLASafetyWithoutType2SolverCanonApproach) HLSolverFactoryCanonApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
            boolean canonApproach = solverCanon.existsWinningStrategy();
            int sizeCanon = solverCanon.getGraph().getStatesView().size();
            int sizeCanonFlows = solverCanon.getGraph().getFlows().size();

//            List<DecisionSet> statesView = new ArrayList<>(solverCanon.getGraph().getStates());
//            Comparator<DecisionSet> comp = new Comparator<>() {
//                @Override
//                public int compare(DecisionSet o1, DecisionSet o2) {
//                    return o1.getIDChain().compareTo(o2.getIDChain());
//                }
//            };
//            Collections.sort(statesView, comp);
//            System.out.println(statesView.size());
//            int a =1;
//            for (DecisionSet decisionSet : statesView) {
//                System.out.println(a++);
////                System.out.println(decisionSet.toString());
//            }
//        System.out.println(SGGBuilderLLCanon.getInstance().dcsOrdered2canon.size());
//        int sizeCanon = -1;
            diff = System.currentTimeMillis() - time;
//        timeLLApproach += diff;        
            double runningTime = Math.round((diff / 1000.0f) * 100.0) / 100.0;
            mean += runningTime;
            Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL strategy canon approach (size " + sizeCanon + "/" + sizeCanonFlows + "): " + canonApproach + " " + runningTime, false, true);
//        HLTools.saveGraph2PDF(outputDir + "hlcanonGGStrat", solverCanon.calculateGraphStrategy());
//        HLTools.saveGraph2PDF(outputDir + "hlcanonGGLLStrat", solverCanon.calculateLLGraphStrategy());
////        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true);
//        PGTools.savePG2PDF(outputDir + "hlcanonPGStrat", solverCanon.getStrategy(), false);
        System.out.println(solverCanon.getGraph().getBadStatesView().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "hlcanonGG", solverCanon.getGraph()); 
            SGGBuilderLLCanon.getInstance().clearBufferedData(); // s.th. like all what is saved in hlgame (like symmetries, and the stuff for the converter) should be cleared
        }

        Logger.getInstance().addMessage("Mapping: " + SGGBuilderLLCanon.getInstance().saveMapping.name(), false, true);
        Logger.getInstance().addMessage("Approach: " + SGGBuilderLLCanon.getInstance().approach.name(), false, true);
        Logger.getInstance().addMessage("Mean: " + mean / ROUNDS, false, true);
//        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% BDD 
//        mean = 0;
//        for (int i = 0; i < ROUNDS; i++) {
////        time = System.currentTimeMillis();
//            BDDSolverOptions opt = new BDDSolverOptions(true);
//            opt.setNoType2(true);
////        HLASafetyWithoutType2CanonRepSolverBDDApproach solver = (HLASafetyWithoutType2CanonRepSolverBDDApproach) HLSolverFactoryBDDApproachCanonReps.getInstance().getSolver(hlgame, opt);
////        solver.getSolver().initialize();
////        boolean canonrep = solver.existsWinningStrategy();
////        diff = System.currentTimeMillis() - time;
////        timeCanonRepApproach += diff;
////        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL strategy CanonReps approach: " + canonrep + " " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
//            time = System.currentTimeMillis();
//            PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true, true);
//            DistrSysBDDSolver<? extends Condition<?>> solverExplBDD = DistrSysBDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(pgame, true, false), opt);
//            Boolean llBDD = solverExplBDD.existsWinningStrategy();
//            diff = System.currentTimeMillis() - time;
//            double runningTime = Math.round((diff / 1000.0f) * 100.0) / 100.0;
//            mean += runningTime;
//            Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL strategy explicit BDD exists: " + llBDD + " " + runningTime, false, true);
////        BDDTools.saveGraph2DotAndPDF(outputDir + "BDDGG", solverExplBDD.getGraphGame(), solverExplBDD);
//        }
//        Logger.getInstance().addMessage("Mean: " + mean / ROUNDS, false, true);

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
        AbstractGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat_low", stratHLlow);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
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
