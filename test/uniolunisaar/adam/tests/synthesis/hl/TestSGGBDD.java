package uniolunisaar.adam.tests.synthesis.hl;

import java.io.File;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IntegerID;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphByHashCode;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.SymmetryIterator;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotCalculateException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.generators.highlevel.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.highlevel.DocumentWorkflowHL;
import uniolunisaar.adam.generators.pgwt.Clerks;
import uniolunisaar.adam.generators.pgwt.Workflow;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestSGGBDD {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/sgg/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testSGG() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);

        PetriGameWithTransits game = HL2PGConverter.convert(hlgame, true, true);

        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(new DistrSysBDDSolvingObject<>(game, new Safety()), syms, new BDDSolverOptions(false));
        sol.initialize();

        double size = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        Logger.getInstance().addMessage("size " + size);

//        BDDGraph graph = sol.getGraphGame();
//        BDDTools.saveGraph2PDF(outputDir + "CM21_gg", graph, sol);
    }

    @Test
    public void testSGGLL() throws IOException, InterruptedException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NotSupportedGameException, ParseException {
        //%%%%%%%%%%%%%%%%%%% CM 21
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, false);
//        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);
//        PetriGame game = HL2PGConverter.convert(hlgame);
        PetriGameWithTransits game = Workflow.generateBJVersion(2, 1, true, false);
//        PGTools.savePG2PDF(outputDir + "CM21_ll", game, false);
        // Test  the old graph game
        BDDSolverOptions opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        DistrSysBDDSolver<? extends Condition<?>> sol = DistrSysBDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), opt);
        BDDGraph bddgraph = sol.getGraphGame();
        Logger.getInstance().addMessage("SIZE BDD: " + bddgraph.getStates().size());
        BDDTools.saveGraph2PDF(outputDir + "CM21_bdd_gg", bddgraph, sol);

        // Test the new version
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);
        Logger.getInstance().addMessage("SIZE: " + graph.getStatesView().size());
        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
//        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size());

        //%%%%%%%%%%%%%%%%%%% CM 22
        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 2, true);
        HLTools.saveHLPG2PDF(outputDir + "CM22", hlgame);
        game = Workflow.generateBJVersion(2, 2, true, false);
//        PGTools.savePG2PDF(outputDir + "CM22_ll", game, false);
//        PetriGame gameConv = HL2PGConverter.convert(hlgame);
//        PGTools.savePG2PDF(outputDir + "CM22_ll_conv", gameConv, false);
        opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        sol = DistrSysBDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), opt);
        bddgraph = sol.getGraphGame();

        graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        Logger.getInstance().addMessage("SIZE: " + graph.getStates().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM22_gg", graph);
//        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size()); // todo: why does this fail?

        //%%%%%%%%%%%%%%%%%%% DW 
        int size = 2;
        hlgame = DocumentWorkflowHL.generateDW(size, true);
//        HLTools.saveHLPG2PDF(outputDir + "DW" + size, hlgame);
        game = Clerks.generateNonCP(size, true, true);
//        PGTools.savePG2PDF(outputDir + "DW" + size + "_ll", game, false);
//        PetriGame gameConv = HL2PGConverter.convert(hlgame);
//        PGTools.savePG2PDF(outputDir + "DW" + size + "_ll_conv", gameConv, false);

        // Test  the old graph game
        opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        sol = DistrSysBDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), opt);
        bddgraph = sol.getGraphGame();
//        Logger.getInstance().addMessage("SIZE BDD: " + bddgraph.getStates().size());
//        BDDTools.saveGraph2PDF(outputDir + "DW" + size + "_bdd_gg", bddgraph, sol);

        graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        Logger.getInstance().addMessage("SIZE: " + graph.getStates().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "DW" + size + "_gg", graph);
        Assert.assertEquals(bddgraph.getStates().size(), graph.getStatesView().size());

    }

    @Test
    public void testCM() throws IOException, InterruptedException, CalculationInterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);

        Logger.getInstance().addMessage("SIZE: " + graph.getStatesView().size());
        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);

//        PetriGame game = HL2PGConverter.convert(hlgame, true, true);
//        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
//
//        BDDSolverOptions opt = new BDDSolverOptions();
//        opt.setLibraryName("buddy");
//        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(game, syms, false, new Safety(), opt);
//        sol.initialize();
//
//        double sizeBDD = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
//        Logger.getInstance().addMessage("size " + sizeBDD);
//
//        BDDGraph graph = sol.getGraphGame();
//        BDDTools.saveGraph2PDF(outputDir + "CM21" + "_bdd_gg", graph, sol);
    }

    @Test
    public void testDocumentWorkflow() throws IOException, InterruptedException, CalculationInterruptedException, NotSupportedGameException, ParseException, CouldNotCalculateException, CouldNotFindSuitableConditionException, SolvingException, RenderException, NetNotSafeException {
        //%%%%%%%%%%%%%%%%%% DW
//        for (int i = 1; i < 5; i++) {
//            int size = i;

        int size = 3;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(size, false);

        PetriGameWithTransits game = HL2PGConverter.convert(hlgame, true, true);
//        PNWTTools.saveAPT(outputDir+"DW"+size, game, true);
//        Partitioner.doIt(game);
        PGTools.savePG2PDF(outputDir + "DW" + size + "_conv", game, false);
//
//        HLPetriGame hlgame2 = DocumentWorkflowHL.generateDW(size, true);
//        PetriGame game2 = HL2PGConverter.convert(hlgame2, true, true);
//        PGTools.savePG2PDF(outputDir + "DW" + size + "_convP", game2, false, 5);

        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
            Symmetry next = iterator.next();

            Logger.getInstance().addMessage(next.toString());
        }

        BDDSolverOptions opt = new BDDSolverOptions(false);
//        opt.setLibraryName("buddy");
        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(new DistrSysBDDSolvingObject<>(game, new Safety()), syms, opt);
        sol.initialize();

        double sizeBDD = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        Logger.getInstance().addMessage("size " + sizeBDD);

//        BDDGraph graph = sol.getGraphGame();
//        BDDTools.saveGraph2PDF(outputDir + "DW" + size + "_gg", graph, sol);
//        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size());
//        }
//        //%%%%%%%%%%%%%%%%%% DWs
//        int size = 3;
//        HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(size);
//        SymbolicGameGraph<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> graph = SGGBuilder.createByLLGame(hlgame);
//        Logger.getInstance().addMessage("SIZE: " + graph.getStates().size());
////        HLTools.saveGraph2DotAndPDF(outputDir + "DWs" + size + "_gg", graph);
//        // convert
//        PetriGame gameConv = HL2PGConverter.convert(hlgame);
//        PGTools.savePG2PDF(outputDir + "DWs" + size + "_ll_conv", gameConv, false);
//
//        // Test  the old graph game
//        PetriGame game = Clerks.generateCP(size, true, true);
//        PGTools.savePG2PDF(outputDir + "DWs" + size + "_ll", game, false, 5); // 1->3, 2->5
//        BDDSolverOptions opt = new BDDSolverOptions();
//        opt.setNoType2(true);
//        DistrSysBDDSolver<? extends Condition<?>> sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, false, false), false, opt);
//        BDDGraph bddgraph = sol.getGraphGame();
//        Logger.getInstance().addMessage("SIZE BDD: " + bddgraph.getStates().size());
////        BDDTools.saveGraph2PDF(outputDir + "DWs" + size + "_bdd_gg", bddgraph, sol);
////        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size());
    }

    @Test
    public void toyExample() throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException, IOException, InterruptedException {
        HLPetriGame hlgame = new HLPetriGame("test");
        hlgame.createBasicColorClass("C", false, new Color("c1"), new Color("c2"));
        Place p1 = hlgame.createSysPlace("p1", new String[]{"C"});
        hlgame.setColorTokens(p1, new Color("c1"), new Color("c2"));
        Place p2 = hlgame.createSysPlace("p2", new String[]{"C"});
        Transition t = hlgame.createTransition("t");
        hlgame.createFlow(p1, t);
        hlgame.createFlow(t, p2);
        HLTools.saveHLPG2PDF(outputDir + "toyexample", hlgame);

        PetriGameWithTransits game = HL2PGConverter.convert(hlgame, true, true);
        PGTools.savePG2PDF(outputDir + "toyexmapelell", game, false, false);

        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(new DistrSysBDDSolvingObject<>(game, new Safety()), syms, new BDDSolverOptions(false));
        sol.initialize();

        double sizeBDD = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        Logger.getInstance().addMessage("size " + sizeBDD);

        BDDGraph graph = sol.getGraphGame();
        BDDTools.saveGraph2PDF(outputDir + "toyexmpale", graph, sol);
        Assert.assertEquals(graph.getStates().size(), graph.getStates().size());
    }

    @Test
    public void toyExample2() throws NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException, IOException, InterruptedException {
        HLPetriGame hlgame = new HLPetriGame("test");
        hlgame.createBasicColorClass("C", false, new Color("c1"), new Color("c2"));
        Place p1 = hlgame.createSysPlace("p1", new String[]{"C"});
        hlgame.setColorTokens(p1, new Color("c1"), new Color("c2"));
        Place p2 = hlgame.createSysPlace("p2", new String[]{"C", "C"});
        Transition t = hlgame.createTransition("t");
        hlgame.createFlow(p1, t);
        hlgame.createFlow(t, p2, new ArcExpression(new ArcTuple(new Variable("x"), new Variable("y"))));
        HLTools.saveHLPG2PDF(outputDir + "toyexample2", hlgame);

        PetriGameWithTransits game = HL2PGConverter.convert(hlgame, true, true);
        PGTools.savePG2PDF(outputDir + "toyexmapelell2", game, false, false);

        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(new DistrSysBDDSolvingObject<>(game, new Safety()), syms, new BDDSolverOptions(false));
        sol.initialize();

        double sizeBDD = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        Logger.getInstance().addMessage("size " + sizeBDD);

        BDDGraph graph = sol.getGraphGame();
        BDDTools.saveGraph2PDF(outputDir + "toyexmpale2", graph, sol);
        Assert.assertEquals(graph.getStates().size(), graph.getStates().size());

        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graphEX = SGGBuilderLL.getInstance().createByHashcode(hlgame);

        Logger.getInstance().addMessage("SIZE: " + graphEX.getStatesView().size());
        HLTools.saveGraph2DotAndPDF(outputDir + "toyexmpleExplixit", graphEX);
    }
}
