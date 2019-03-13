package uniolunisaar.adam.tests.hl;

import java.io.File;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.CommitmentSet;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestSRG {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/converter/";

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
        CommitmentSet c1 = new CommitmentSet(true);
        CommitmentSet c2 = new CommitmentSet(true);
        Assert.assertEquals(c1, c2);
        CommitmentSet c3 = new CommitmentSet(false);
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
        CommitmentSet c4 = new CommitmentSet(ct1, ct2, ct3);
        CommitmentSet c5 = new CommitmentSet(ct1, ct2, ct3);
        Assert.assertEquals(c4, c5);

        ct1 = new ColoredTransition(game, t1, new Valuation());
        ct2 = new ColoredTransition(game, t2, valt2);
        ct3 = new ColoredTransition(game, t3, valt3);
        CommitmentSet c6 = new CommitmentSet(ct1, ct2, ct3);
        Assert.assertEquals(c5, c6);

        ct1 = new ColoredTransition(game, game.getTransition("t0"), new Valuation());
        valt2 = new Valuation();
        valt2.put(new Variable("x"), new Color("c"));
        ct2 = new ColoredTransition(game, game.getTransition("asdf"), valt2);
        ct3 = new ColoredTransition(game, game.getTransition("asdf2"), valt3);
        CommitmentSet c7 = new CommitmentSet(ct3, ct2, ct1);
        Assert.assertEquals(c6, c7);
    }
}
