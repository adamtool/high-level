package uniolunisaar.adam.tests.synthesis.hl.pnml;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.generators.highlevel.AlarmSystemHL;
import uniolunisaar.adam.generators.highlevel.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.highlevel.ContainerHabourHL;
import uniolunisaar.adam.generators.highlevel.DocumentWorkflowHL;
import uniolunisaar.adam.generators.highlevel.PackageDeliveryHL;
import uniolunisaar.adam.tools.Logger;

import java.util.*;

import static uniolunisaar.adam.tests.synthesis.hl.pnml.HighLevelSymmetricPetriNetsTests.doAllTests;
import static uniolunisaar.adam.tests.synthesis.hl.pnml.HighLevelSymmetricPetriNetsTests.render;

@Test
public class TestHighLevelRendererAndParserOnGeneratedGames {

    private static final Logger log = Logger.getInstance();

    @BeforeClass
    public void configureLogger() {
        log.setVerbose(false);
        log.setWarningStream(null);
    }

    private static void testGame(HLPetriGame game, String... exclude) throws Exception {
        doAllTests(game, render(game), List.of(exclude));
    }

    @Test
    public void testAlarmSystem() throws Exception {
        testGame(AlarmSystemHL.createSafetyVersionForHLRep(2, true));
    }

    @Test
    public void testConcurrentMachines() throws Exception {
        testGame(ConcurrentMachinesHL.generateImprovedVersion(4, 1, true));
        testGame(ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(4, 1, true));
    }

    @Test
    public void testContainerHarbour() throws Exception {
        // isomorphism moderately slow (~5m)
        testGame(ContainerHabourHL.generateD(4, 2, 1, 1, true), "-isomorphism");
    }

    @Test
    public void testDocumentWorkflow() throws Exception {
        testGame(DocumentWorkflowHL.generateDW(4, true));
        testGame(DocumentWorkflowHL.generateDWs(4, true));
    }

    @Test
    public void testPackageDelivery() throws Exception {
        testGame(PackageDeliveryHL.generateE(2, 1, true));
    }
}
