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
import uniolunisaar.adam.ds.graph.hl.CommitmentSet;
import uniolunisaar.adam.ds.graph.hl.SRGFlow;
import uniolunisaar.adam.ds.graph.hl.SymbolicGameGraph;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLCommitmentSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLSysDecision;
import uniolunisaar.adam.ds.graph.hl.approachHL.IHLDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.ILLDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLCommitmentSet;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLEnvDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLSysDecision;
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
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.hl.DocumentWorkflowHL;
import uniolunisaar.adam.generators.pg.Clerks;
import uniolunisaar.adam.generators.pg.Workflow;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.hl.SGGBuilder;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;
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
    public void testCommitmentSets() {
        CommitmentSet<ColoredTransition> c1 = new HLCommitmentSet(true);
        CommitmentSet<ColoredTransition> c2 = new HLCommitmentSet(true);
        Assert.assertEquals(c1, c2);
        CommitmentSet<ColoredTransition> c3 = new HLCommitmentSet(false);
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
        CommitmentSet<ColoredTransition> c4 = new HLCommitmentSet(ct1, ct2, ct3);
        CommitmentSet<ColoredTransition> c5 = new HLCommitmentSet(ct1, ct2, ct3);
        Assert.assertEquals(c4, c5);

        ct1 = new ColoredTransition(game, t1, new Valuation());
        ct2 = new ColoredTransition(game, t2, valt2);
        ct3 = new ColoredTransition(game, t3, valt3);
        CommitmentSet<ColoredTransition> c6 = new HLCommitmentSet(ct1, ct2, ct3);
        Assert.assertEquals(c5, c6);

        ct1 = new ColoredTransition(game, game.getTransition("t0"), new Valuation());
        valt2 = new Valuation();
        valt2.put(new Variable("x"), new Color("c"));
        ct2 = new ColoredTransition(game, game.getTransition("asdf"), valt2);
        ct3 = new ColoredTransition(game, game.getTransition("asdf2"), valt3);
        CommitmentSet<ColoredTransition> c7 = new HLCommitmentSet(ct3, ct2, ct1);
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
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1);
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
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1);
        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame);
        SymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> graph = SGGBuilder.createByHLGame(game);
        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
        System.out.println("SIZE: " + graph.getStates().size());
    }

    @Test
    public void testSGGLL() throws IOException, InterruptedException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, NotSupportedGameException, ParseException {
        //%%%%%%%%%%%%%%%%%%% CM 21
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1);
//        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);
//        PetriGame game = HL2PGConverter.convert(hlgame);
        PetriGame game = Workflow.generateBJVersion(2, 1, true, false);
//        PGTools.savePG2PDF(outputDir + "CM21_ll", game, false);
        // Test  the old graph game
        BDDSolverOptions opt = new BDDSolverOptions();
        opt.setNoType2(true);
        BDDSolver<? extends Condition> sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), true, opt);
        BDDGraph bddgraph = sol.getGraphGame();
//        System.out.println("SIZE BDD: " + bddgraph.getStates().size());
//        BDDTools.saveGraph2PDF(outputDir + "CM21_bdd_gg", bddgraph, sol);

        // Test the new version
        SymbolicGameGraph<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> graph = SGGBuilder.createByLLGame(hlgame);
//        System.out.println("SIZE: " + graph.getStates().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size());

        //%%%%%%%%%%%%%%%%%%% CM 22
        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 2);
        HLTools.saveHLPG2PDF(outputDir + "CM22", hlgame);
        game = Workflow.generateBJVersion(2, 2, true, false);
//        PGTools.savePG2PDF(outputDir + "CM22_ll", game, false);
//        PetriGame gameConv = HL2PGConverter.convert(hlgame);
//        PGTools.savePG2PDF(outputDir + "CM22_ll_conv", gameConv, false);
        opt = new BDDSolverOptions();
        opt.setNoType2(true);
        sol = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(game, true, false), true, opt);
        bddgraph = sol.getGraphGame();

        graph = SGGBuilder.createByLLGame(hlgame);
//        System.out.println("SIZE: " + graph.getStates().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM22_gg", graph);
//        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size()); // todo: why does this fail?

        //%%%%%%%%%%%%%%%%%%% DW 
        int size = 2;
        hlgame = DocumentWorkflowHL.generateDW(size);
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

        graph = SGGBuilder.createByLLGame(hlgame);
//        System.out.println("SIZE: " + graph.getStates().size());
//        HLTools.saveGraph2DotAndPDF(outputDir + "DW" + size + "_gg", graph);
        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size());

    }

    @Test
    public void testCM() throws IOException, InterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1);
        SymbolicGameGraph<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> graph = SGGBuilder.createByLLGame(hlgame);

        System.out.println("SIZE: " + graph.getStates().size());
        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
    }

    @Test
    public void testDocumentWorkflow() throws IOException, InterruptedException {
        int size = 2;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(size);
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
            Symmetry next = iterator.next();
            System.out.println(next.toString());
        }
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
        SymbolicGameGraph<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> graph = SGGBuilder.createByLLGame(hlgame);

        System.out.println("SIZE: " + graph.getStates().size());
        HLTools.saveGraph2DotAndPDF(outputDir + "DW" + size + "_gg", graph);
//        Assert.assertEquals(bddgraph.getStates().size(), graph.getStates().size());
    }
}
