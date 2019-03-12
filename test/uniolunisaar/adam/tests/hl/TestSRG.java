package uniolunisaar.adam.tests.hl;

import java.io.File;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.CommitmentSet;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;

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
        Transition t2 = game.createTransition("asdf");
        Transition t3 = game.createTransition("asdf2");
        CommitmentSet c4 = new CommitmentSet(t1, t2, t3);
        CommitmentSet c5 = new CommitmentSet(t1, t2, t3);
        Assert.assertEquals(c4, c5);
        CommitmentSet c6 = new CommitmentSet(game.getTransitions());
        Assert.assertEquals(c5, c6);
        CommitmentSet c7 = new CommitmentSet(game.getTransition("asdf2"), game.getTransition("asdf"), game.getTransition("t0"));
        Assert.assertEquals(c6, c7);
        t1.setLabel("peter");
        Assert.assertEquals(c6, c7);
        CommitmentSet c8 = new CommitmentSet(t1, t2, t3);
        Assert.assertEquals(c4, c8);
        CommitmentSet c9 = new CommitmentSet(game.getTransition("asdf2"), t2, game.getTransition("t0"));
        Assert.assertEquals(c4, c9);

    }
}
