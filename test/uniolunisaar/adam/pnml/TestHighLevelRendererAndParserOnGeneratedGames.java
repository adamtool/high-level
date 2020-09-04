package uniolunisaar.adam.pnml;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.hl.ContainerHabourHL;
import uniolunisaar.adam.generators.hl.DocumentWorkflowHL;
import uniolunisaar.adam.generators.hl.PackageDeliveryHL;
import uniolunisaar.adam.tools.Logger;

import java.util.*;

import static uniolunisaar.adam.pnml.HighLevelSymmetricPetriNetsTests.doAllTests;
import static uniolunisaar.adam.pnml.HighLevelSymmetricPetriNetsTests.render;

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
