package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.ParseException;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.graph.AbstractCommitmentSet;
import uniolunisaar.adam.ds.graph.IntegerID;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.GameGraphByHashCode;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLSysDecision;
import uniolunisaar.adam.ds.graph.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLCommitmentSet;
import uniolunisaar.adam.ds.graph.hl.llapproach.LLCommitmentSet;
import uniolunisaar.adam.ds.graph.hl.llapproach.LLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.llapproach.LLEnvDecision;
import uniolunisaar.adam.ds.graph.hl.llapproach.LLSysDecision;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.highlevel.symmetries.RotationIterator;
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
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.hl.DocumentWorkflowHL;
import uniolunisaar.adam.generators.hl.PackageDeliveryHL;
import uniolunisaar.adam.generators.pg.Clerks;
import uniolunisaar.adam.generators.pg.Workflow;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.pg.solver.hl.bddapproach.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolver;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverFactory;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestSRG {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/srg/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testHashCode() {
        String a = "ERR_m1";
        String b = "OK_m0";
//        System.out.println(a.hashCode());
//        System.out.println(b.hashCode());        
        String c = "ERR_m0";
        String d = "OK_m1";
//        System.out.println(c.hashCode());
//        System.out.println(d.hashCode());      
        boolean equals = a.hashCode() + b.hashCode() == c.hashCode() + d.hashCode();
        Assert.assertTrue(equals); // this is a problem when not using extra hashcode functions
        PetriGame game = new PetriGame("test");
        List<Color> col = new ArrayList<>();
        col.add(new Color("m0"));
        List<Color> col1 = new ArrayList<>();
        col1.add(new Color("m1"));
        Place pA = game.createPlace(a);
        HL2PGConverter.setColorsAndID2Extension(pA, "ERR", col1);
        Place pB = game.createPlace(b);
        HL2PGConverter.setColorsAndID2Extension(pB, "OK", col);
        Place pC = game.createPlace(c);
        HL2PGConverter.setColorsAndID2Extension(pC, "ERR", col);
        Place pD = game.createPlace(d);
        HL2PGConverter.setColorsAndID2Extension(pD, "OK", col1);
        equals = HL2PGConverter.getHashCode(pA) + HL2PGConverter.getHashCode(pB) == HL2PGConverter.getHashCode(pC) + HL2PGConverter.getHashCode(pD);
        System.out.println(HL2PGConverter.getHashCode(pA));
        System.out.println(HL2PGConverter.getHashCode(pB));
        System.out.println(HL2PGConverter.getHashCode(pC));
        System.out.println(HL2PGConverter.getHashCode(pD));
        Assert.assertFalse(equals);
    }

    @Test
    public void testCommitmentSets() {
        AbstractCommitmentSet<ColoredTransition> c1 = new HLCommitmentSet(true);
        AbstractCommitmentSet<ColoredTransition> c2 = new HLCommitmentSet(true);
        Assert.assertEquals(c1, c2);
        AbstractCommitmentSet<ColoredTransition> c3 = new HLCommitmentSet(false);
        Assert.assertTrue(!c1.equals(c3));

        HLPetriGame game = new HLPetriGame("testing");
        Transition t1 = game.createTransition();
        ColoredTransition ct1 = new ColoredTransition(game, t1, new Valuation());
        Transition t2 = game.createTransition("asdf");
        Valuation valt2 = new Valuation();
        valt2.put(new Variable("x"), new Color("c"));
        ColoredTransition ct2 = new ColoredTransition(game, t2, valt2);
        Valuation valt3 = new Valuation();
        valt3.put(new Variable("y"), new Color("c3"));
        Transition t3 = game.createTransition("asdf2");
        ColoredTransition ct3 = new ColoredTransition(game, t3, valt3);
        AbstractCommitmentSet<ColoredTransition> c4 = new HLCommitmentSet(ct1, ct2, ct3);
        AbstractCommitmentSet<ColoredTransition> c5 = new HLCommitmentSet(ct1, ct2, ct3);
        Assert.assertEquals(c4, c5);

        ct1 = new ColoredTransition(game, t1, new Valuation());
        ct2 = new ColoredTransition(game, t2, valt2);
        ct3 = new ColoredTransition(game, t3, valt3);
        AbstractCommitmentSet<ColoredTransition> c6 = new HLCommitmentSet(ct1, ct2, ct3);
        Assert.assertEquals(c5, c6);

        ct1 = new ColoredTransition(game, game.getTransition("t0"), new Valuation());
        valt2 = new Valuation();
        valt2.put(new Variable("x"), new Color("c"));
        ct2 = new ColoredTransition(game, game.getTransition("asdf"), valt2);
        ct3 = new ColoredTransition(game, game.getTransition("asdf2"), valt3);
        AbstractCommitmentSet<ColoredTransition> c7 = new HLCommitmentSet(ct3, ct2, ct1);
        Assert.assertEquals(c6, c7);
    }

    @Test
    public void testRotation() {
        List<Color> colors = new ArrayList<>();
        colors.add(new Color("0"));
        colors.add(new Color("1"));
        colors.add(new Color("2"));
        colors.add(new Color("3"));
        colors.add(new Color("4"));
        List<List<Color>> rotations = new ArrayList<>();
        for (RotationIterator it = new RotationIterator(colors); it.hasNext();) {
            List<Color> rot = it.next();
            rotations.add(rot);
        }
        Assert.assertEquals(rotations.toString(), "[[0, 1, 2, 3, 4], "
                + "[1, 2, 3, 4, 0], "
                + "[2, 3, 4, 0, 1], "
                + "[3, 4, 0, 1, 2], "
                + "[4, 0, 1, 2, 3]]");
    }

    @Test
    public void testChoosen() {
        HLPetriGame game = new HLPetriGame("tesing");
        Transition t1 = game.createTransition();
        ColoredTransition ct1 = new ColoredTransition(game, t1, new Valuation());

        Transition t2 = game.createTransition("asdf");
        Valuation valt2 = new Valuation();
        valt2.put(new Variable("x"), new Color("c"));
        valt2.put(new Variable("z"), new Color("d"));
        ColoredTransition ct2 = new ColoredTransition(game, t2, valt2);

        Transition t3 = game.createTransition("asdf2");
        Valuation valt3 = new Valuation();
        valt3.put(new Variable("y"), new Color("c3"));
        ColoredTransition ct3 = new ColoredTransition(game, t3, valt3);

        HLCommitmentSet c1 = new HLCommitmentSet(ct1, ct2, ct3);
        Assert.assertTrue(c1.isChoosen(ct1));
        Assert.assertTrue(c1.isChoosen(ct2));
        Assert.assertTrue(c1.isChoosen(ct3));
        HLCommitmentSet c2 = new HLCommitmentSet(ct1, ct3);
        Assert.assertTrue(c2.isChoosen(ct1));
        Assert.assertTrue(!c2.isChoosen(ct2));
        Assert.assertTrue(c2.isChoosen(ct3));

        Valuation valt2Equal = new Valuation();
        valt2Equal.put(new Variable("x"), new Color("c"));
        valt2Equal.put(new Variable("z"), new Color("d"));
        Assert.assertEquals(valt2, valt2Equal);
        ColoredTransition ct2Equal = new ColoredTransition(game, t2, valt2Equal);
        Assert.assertEquals(ct2, ct2Equal);
        ct2Equal = new ColoredTransition(game, game.getTransition("asdf"), valt2Equal);
        Assert.assertEquals(ct2, ct2Equal);

        game.createBasicColorClass("class", false, "c");
        HLSysDecision dc = new HLSysDecision(game.createSysPlace("id", new String[]{"class"}), new ColorToken(new Color("c")), c1);
        Assert.assertTrue(dc.isChoosen(ct1));
        Assert.assertTrue(dc.isChoosen(ct2Equal));
        Assert.assertTrue(dc.isChoosen(ct3));

    }

    @Test
    public void testSymmetries() throws FileNotFoundException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);

        PetriGame game = HL2PGConverter.convert(hlgame, true);
//        for (Place place : game.getPlaces()) {
//            System.out.println(place.getId() +" col " +HL2PGConverter.getColors(place).toString());
//        }

        // Create the iterator for the symmetries
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        // Test EnvDecision
        LLEnvDecision a = new LLEnvDecision(game, game.getPlace("OK_m1"));
        LLEnvDecision b = new LLEnvDecision(game, game.getPlace("OK_m0"));
        Assert.assertTrue(checkSym(syms, a, b));

        // Test SysDecision
        LLCommitmentSet c1 = new LLCommitmentSet(game, true);
        LLCommitmentSet c2 = new LLCommitmentSet(game, true);
        LLSysDecision x = new LLSysDecision(game, game.getPlace("OK_m1"), c1);
        LLSysDecision y = new LLSysDecision(game, game.getPlace("OK_m0"), c2);
        Assert.assertTrue(checkSym(syms, x, y));

        c1 = new LLCommitmentSet(game, game.getTransition("d_e_em_m1"));
        c2 = new LLCommitmentSet(game, game.getTransition("d_e_em_m0"));
        LLSysDecision x1 = new LLSysDecision(game, game.getPlace("OK_m1"), c1);
        LLSysDecision y1 = new LLSysDecision(game, game.getPlace("OK_m0"), c2);
        Assert.assertTrue(checkSym(syms, x1, y1));

        c1 = new LLCommitmentSet(game, game.getTransition("d_e_em_m1"), game.getTransition("test_m_m1"));
        c2 = new LLCommitmentSet(game, game.getTransition("test_m_m0"), game.getTransition("d_e_em_m0"));
        LLSysDecision x2 = new LLSysDecision(game, game.getPlace("OK_m1"), c1);
        LLSysDecision y2 = new LLSysDecision(game, game.getPlace("OK_m0"), c2);
        Assert.assertTrue(checkSym(syms, x2, y2));

        // Test DecisionSets
        Set<ILLDecision> dcss1 = new HashSet<>();
        dcss1.add(a);
        dcss1.add(x);
        LLDecisionSet dcs1 = new LLDecisionSet(dcss1, true, false, game);
        Set<ILLDecision> dcss2 = new HashSet<>();
        dcss2.add(b);
        dcss2.add(y);
        LLDecisionSet dcs2 = new LLDecisionSet(dcss2, true, false, game);
        Assert.assertTrue(checkSym(syms, dcs1, dcs2));

        dcss1.add(x1);
        dcss1.add(x2);
        dcss2.add(y1);
        dcss2.add(y2);
        dcs1 = new LLDecisionSet(dcss1, true, false, game);
        dcs2 = new LLDecisionSet(dcss2, true, false, game);
        Assert.assertTrue(checkSym(syms, dcs1, dcs2));

        // Test copies
        LLDecisionSet copy1 = new LLDecisionSet(dcs1);
        LLDecisionSet copy2 = new LLDecisionSet(dcs2);
        Assert.assertTrue(checkSym(syms, copy1, copy2));
        Assert.assertTrue(checkSym(syms, dcs1, copy1));
        Assert.assertTrue(checkSym(syms, dcs2, copy2));
        Assert.assertTrue(checkSym(syms, copy2, copy1));
        Assert.assertTrue(checkSym(syms, copy1, dcs1));
        Assert.assertTrue(checkSym(syms, copy2, dcs2));
        Assert.assertTrue(checkSym(syms, copy2, dcs1));

    }

    private boolean checkSym(Symmetries syms, ILLDecision a, ILLDecision b) {
        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
            Symmetry sym = iterator.next();
            a.apply(sym);
            if (a.equals(b)) {
//                System.out.println(sym.toString());
                return true;
            }
        }
        return false;
    }

    private boolean checkSym(Symmetries syms, LLDecisionSet a, LLDecisionSet b) {
        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
            Symmetry sym = iterator.next();
            a.apply(sym);
            if (a.equals(b)) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void testSGG() throws IOException, InterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        GameGraphByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
        System.out.println("SIZE: " + graph.getStatesView().size());
    }

    @Test
    public void testSGGLL() throws IOException, InterruptedException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NotSupportedGameException, ParseException {
        //%%%%%%%%%%%%%%%%%%% CM 21
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);
//        PetriGame game = HL2PGConverter.convert(hlgame);
        PetriGame game = Workflow.generateBJVersion(2, 1, true, false);
//        PGTools.savePG2PDF(outputDir + "CM21_ll", game, false);
        // Test  the old graph game
        BDDSolverOptions opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        BDDSolver<? extends Condition<?>> sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), opt);
        BDDGraph bddgraph = sol.getGraphGame();
//        System.out.println("SIZE BDD: " + bddgraph.getStates().size());
//        BDDTools.saveGraph2PDF(outputDir + "CM21_bdd_gg", bddgraph, sol);

        // Test the new version
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        System.out.println("SIZE: " + graph.getStates().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
        Assert.assertEquals(bddgraph.getStates().size(), graph.getStatesView().size());

        //%%%%%%%%%%%%%%%%%%% CM 22
        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 2, true);
        HLTools.saveHLPG2PDF(outputDir + "CM22", hlgame);
        game = Workflow.generateBJVersion(2, 2, true, false);
//        PGTools.savePG2PDF(outputDir + "CM22_ll", game, false);
//        PetriGame gameConv = HL2PGConverter.convert(hlgame);
//        PGTools.savePG2PDF(outputDir + "CM22_ll_conv", gameConv, false);
        opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), opt);
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
        opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), opt);
        bddgraph = sol.getGraphGame();
//        System.out.println("SIZE BDD: " + bddgraph.getStates().size());
//        BDDTools.saveGraph2PDF(outputDir + "DW" + size + "_bdd_gg", bddgraph, sol);

        graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        System.out.println("SIZE: " + graph.getStates().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "DW" + size + "_gg", graph);
        Assert.assertEquals(bddgraph.getStates().size(), graph.getStatesView().size());

    }

    @Test
    public void testCM() throws IOException, InterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);

        System.out.println("SIZE: " + graph.getStatesView().size());
        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
    }

    @Test
    public void testDocumentWorkflow() throws IOException, InterruptedException, CalculationInterruptedException, NotSupportedGameException, ParseException, CouldNotCalculateException, CouldNotFindSuitableConditionException, SolvingException {
        //%%%%%%%%%%%%%%%%%% DW
//        for (int i = 1; i < 5; i++) {
//            int size = i;
        int size = 2;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(size, true);
//        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
//        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
//            Symmetry next = iterator.next();
//            System.out.println(next.toString());
//        }
//        HLTools.saveHLPG2PDF(outputDir + "DW" + size, hlgame);
//        game = Clerks.generateNonCP(size, true, true);
//        PGTools.savePG2PDF(outputDir + "DW" + size + "_ll", game, false);
//        PetriGame gameConv = HL2PGConverter.convert(hlgame);
//        PGTools.savePG2PDF(outputDir + "DW" + size + "_ll_conv", gameConv, false);

//        // Test  the old graph game
//        opt = new BDDSolverOptions();
//        opt.setNoType2(true);
//        sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), true, opt);
//        bddgraph = sol.getGraphGame();
////        System.out.println("SIZE BDD: " + bddgraph.getStates().size());
//        BDDTools.saveGraph2PDF(outputDir + "DW" + size + "_bdd_gg", bddgraph, sol);
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);

        System.out.println("SIZE: " + graph.getStatesView().size());
        HLTools.saveGraph2DotAndPDF(outputDir + "DW" + size + "_gg", graph);
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
//        BDDSolver<? extends Condition<?>> sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, false, false), false, opt);
//        BDDGraph bddgraph = sol.getGraphGame();
//        System.out.println("SIZE BDD: " + bddgraph.getStates().size());
////        BDDTools.saveGraph2PDF(outputDir + "DWs" + size + "_bdd_gg", bddgraph, sol);
////        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size());
    }

    @Test
    public void packageDelivery() throws ModuleException, CalculationInterruptedException, IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, ParseException, CouldNotCalculateException, CouldNotFindSuitableConditionException, SolvingException {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setVerboseMessageStream(null);
        HLPetriGame hlgame = PackageDeliveryHL.generateEwithPool(2, 2, true);
        HLTools.saveHLPG2PDF(outputDir + "PDPG11", hlgame);

//        // %%%%%%%%%%%%%%%%%%%%%% HL VERSION        
//        OneEnvHLPG game = new OneEnvHLPG(hlgame);
//        HLTools.saveHLPG2PDF(outputDir + "PDPG11_oneEnv", game);
//
//        SymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> hlgraph = SGGBuilder.createByHLGame(game);
//        int size = hlgraph.getStates().size();
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HLSGG: " + size);
//        HLTools.saveGraph2PDF(outputDir + "PDHLExHL11_gg", hlgraph);
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);

        int size = graph.getStatesView().size();
//        System.out.println("Number of states of the HL two-player game over a finite graph explizit: " + size);
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + size);
//        HLTools.saveGraph2PDF(outputDir + "PDHLExLL11_gg", graph);

        PetriGame pg = HL2PGConverter.convert(hlgame, true, true);
        PGTools.savePG2PDF(outputDir + "PDLL11", pg, false);
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
//        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
//            Symmetry next = iterator.next();
//            System.out.println(next.toString());
//        }
        BDDSolverOptions opt = new BDDSolverOptions(false);
        opt.setNoType2(true);
        BDDASafetyWithoutType2HLSolver solBDD = new BDDASafetyWithoutType2HLSolver(new BDDSolvingObject<>(pg, new Safety()), syms, opt);
        solBDD.initialize();

        double sizeBDD = solBDD.getBufferedDCSs().satCount(solBDD.getFirstBDDVariables()) + 1;
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% size " + sizeBDD);
        System.out.println("asdf " + solBDD.existsWinningStrategy());
        BDDGraph bddgraphHL = solBDD.getGraphGame();
//        BDDTools.saveGraph2PDF(outputDir + "PDHL13_gg", bddgraphHL, solBDD);

        opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        BDDSolver solBDDLL = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(pg, true, false), opt);
        solBDDLL.initialize();
        double sizeBDDLL = solBDDLL.getBufferedDCSs().satCount(solBDD.getFirstBDDVariables()) + 1;
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% size " + sizeBDDLL);
    }

    @Test
    public void testDifferentSizes() throws ModuleException {
        Logger.getInstance().setVerbose(false);
        Logger.getInstance().setVerboseMessageStream(null);
        String idInput = "031519_DW_GS/2_DW";
        String id = idInput.substring(idInput.lastIndexOf("/") + 1);
        String[] elem = id.split("_");
        int[] para = new int[elem.length - 1];
        for (int i = 0; i < elem.length - 1; i++) {
            para[i] = Integer.parseInt(elem[i]);
        }
        HLPetriGame hlgame = getHLGame(elem[elem.length - 1], para);

        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(hlgame);

        int size = graph.getStatesView().size();
        System.out.println("Number of states of the HL two-player game over a finite graph explizit: " + size); // todo: fix the logger...
    }

    private HLPetriGame getHLGame(String id, int[] paras) throws ModuleException {
        switch (id) {
            case "AS":
                return AlarmSystemHL.createSafetyVersionForHLRepWithSetMinus(paras[0], true);
            case "CM":
                return ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(paras[0], paras[1], true);
            case "DW":
                return DocumentWorkflowHL.generateDW(paras[0], true);
            case "DWs":
                return DocumentWorkflowHL.generateDWs(paras[0], true);
            default:
                throw new ModuleException("Benchmark " + id + " not yet implemented.");
        }
    }
}
