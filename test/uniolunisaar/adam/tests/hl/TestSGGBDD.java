package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.graph.hl.IntegerID;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.graph.hl.SGGByHashCode;
import uniolunisaar.adam.ds.graph.hl.approachLL.ILLDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLDecisionSet;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.highlevel.symmetries.SymmetryIterator;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.CouldNotCalculateException;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.hl.DocumentWorkflowHL;
import uniolunisaar.adam.generators.pg.Clerks;
import uniolunisaar.adam.generators.pg.Workflow;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.solver.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
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
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testSGG() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);

        PetriGame game = HL2PGConverter.convert(hlgame, true, true);

        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(game, syms, false, new Safety(), new BDDSolverOptions());
        sol.initialize();

        double size = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        System.out.println("size " + size);

//        BDDGraph graph = sol.getGraphGame();
//        BDDTools.saveGraph2PDF(outputDir + "CM21_gg", graph, sol);
    }

    @Test
    public void testSGGLL() throws IOException, InterruptedException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NotSupportedGameException, ParseException {
        //%%%%%%%%%%%%%%%%%%% CM 21
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, false);
//        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);
//        PetriGame game = HL2PGConverter.convert(hlgame);
        PetriGame game = Workflow.generateBJVersion(2, 1, true, false);
//        PGTools.savePG2PDF(outputDir + "CM21_ll", game, false);
        // Test  the old graph game
        BDDSolverOptions opt = new BDDSolverOptions();
        opt.setNoType2(true);
        BDDSolver<? extends Condition> sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), true, opt);
        BDDGraph bddgraph = sol.getGraphGame();
        System.out.println("SIZE BDD: " + bddgraph.getStates().size());
        BDDTools.saveGraph2PDF(outputDir + "CM21_bdd_gg", bddgraph, sol);

        // Test the new version
        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);
        System.out.println("SIZE: " + graph.getStatesView().size());
        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
//        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size());

        //%%%%%%%%%%%%%%%%%%% CM 22
        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 2, true);
        HLTools.saveHLPG2PDF(outputDir + "CM22", hlgame);
        game = Workflow.generateBJVersion(2, 2, true, false);
//        PGTools.savePG2PDF(outputDir + "CM22_ll", game, false);
//        PetriGame gameConv = HL2PGConverter.convert(hlgame);
//        PGTools.savePG2PDF(outputDir + "CM22_ll_conv", gameConv, false);
        opt = new BDDSolverOptions();
        opt.setNoType2(true);
        sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), true, opt);
        bddgraph = sol.getGraphGame();

        graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        System.out.println("SIZE: " + graph.getStates().size());
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
        opt = new BDDSolverOptions();
        opt.setNoType2(true);
        sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), true, opt);
        bddgraph = sol.getGraphGame();
//        System.out.println("SIZE BDD: " + bddgraph.getStates().size());
//        BDDTools.saveGraph2PDF(outputDir + "DW" + size + "_bdd_gg", bddgraph, sol);

        graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        System.out.println("SIZE: " + graph.getStates().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "DW" + size + "_gg", graph);
        Assert.assertEquals(bddgraph.getStates().size(), graph.getStatesView().size());

    }

    @Test
    public void testCM() throws IOException, InterruptedException, CalculationInterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);

        System.out.println("SIZE: " + graph.getStatesView().size());
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
//        System.out.println("size " + sizeBDD);
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

        PetriGame game = HL2PGConverter.convert(hlgame, true, true);
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

            System.out.println(next.toString());
        }

        BDDSolverOptions opt = new BDDSolverOptions();
//        opt.setLibraryName("buddy");
        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(game, syms, false, new Safety(), opt);
        sol.initialize();

        double sizeBDD = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        System.out.println("size " + sizeBDD);

//        BDDGraph graph = sol.getGraphGame();
//        BDDTools.saveGraph2PDF(outputDir + "DW" + size + "_gg", graph, sol);
//        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size());
//        }
//        //%%%%%%%%%%%%%%%%%% DWs
//        int size = 3;
//        HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(size);
//        SymbolicGameGraph<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> graph = SGGBuilder.createByLLGame(hlgame);
//        System.out.println("SIZE: " + graph.getStates().size());
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
//        BDDSolver<? extends Condition> sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, false, false), false, opt);
//        BDDGraph bddgraph = sol.getGraphGame();
//        System.out.println("SIZE BDD: " + bddgraph.getStates().size());
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

        PetriGame game = HL2PGConverter.convert(hlgame, true, true);
        PGTools.savePG2PDF(outputDir + "toyexmapelell", game, false, false);

        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(game, syms, false, new Safety(), new BDDSolverOptions());
        sol.initialize();

        double sizeBDD = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        System.out.println("size " + sizeBDD);

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

        PetriGame game = HL2PGConverter.convert(hlgame, true, true);
        PGTools.savePG2PDF(outputDir + "toyexmapelell2", game, false, false);

        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(game, syms, false, new Safety(), new BDDSolverOptions());
        sol.initialize();

        double sizeBDD = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        System.out.println("size " + sizeBDD);

        BDDGraph graph = sol.getGraphGame();
        BDDTools.saveGraph2PDF(outputDir + "toyexmpale2", graph, sol);
        Assert.assertEquals(graph.getStates().size(), graph.getStates().size());

        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graphEX = SGGBuilderLL.getInstance().createByHashcode(hlgame);

        System.out.println("SIZE: " + graphEX.getStatesView().size());
        HLTools.saveGraph2DotAndPDF(outputDir + "toyexmpleExplixit", graphEX);
    }
}
