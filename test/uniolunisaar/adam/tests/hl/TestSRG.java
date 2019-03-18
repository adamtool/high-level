package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.CommitmentSet;
import uniolunisaar.adam.ds.graph.hl.SRGFlow;
import uniolunisaar.adam.ds.graph.hl.SymbolicGameGraph;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLCommitmentSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLSysDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLDecisionSet;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.highlevel.symmetries.RotationIterator;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.logic.hl.SGGBuilder;
import uniolunisaar.adam.util.HLTools;

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
        CommitmentSet c1 = new HLCommitmentSet(true);
        CommitmentSet c2 = new HLCommitmentSet(true);
        Assert.assertEquals(c1, c2);
        CommitmentSet c3 = new HLCommitmentSet(false);
        Assert.assertTrue(!c1.equals(c3));

        HLPetriGame game = new HLPetriGame("tesing");
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
        CommitmentSet c4 = new HLCommitmentSet(ct1, ct2, ct3);
        CommitmentSet c5 = new HLCommitmentSet(ct1, ct2, ct3);
        Assert.assertEquals(c4, c5);

        ct1 = new ColoredTransition(game, t1, new Valuation());
        ct2 = new ColoredTransition(game, t2, valt2);
        ct3 = new ColoredTransition(game, t3, valt3);
        CommitmentSet c6 = new HLCommitmentSet(ct1, ct2, ct3);
        Assert.assertEquals(c5, c6);

        ct1 = new ColoredTransition(game, game.getTransition("t0"), new Valuation());
        valt2 = new Valuation();
        valt2.put(new Variable("x"), new Color("c"));
        ct2 = new ColoredTransition(game, game.getTransition("asdf"), valt2);
        ct3 = new ColoredTransition(game, game.getTransition("asdf2"), valt3);
        CommitmentSet c7 = new HLCommitmentSet(ct3, ct2, ct1);
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
    public void testSRG() throws IOException, InterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1);
        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame);
//        SymbolicGameGraph<HLDecisionSet, SRGFlow<ColoredTransition>> graph = SGGBuilder.createByHLGame(game);
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
//        System.out.println("SIZE: " + graph.getStates().size());
    }

    @Test
    public void testSGGLL() throws IOException, InterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1);
        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);
        SymbolicGameGraph<LLDecisionSet, SRGFlow<Transition>> graph = SGGBuilder.createByLLGame(hlgame);
        HLTools.saveGraph2DotAndPDF(outputDir + "CM21_gg", graph);
        System.out.println("SIZE: " + graph.getStates().size());
    }
}
