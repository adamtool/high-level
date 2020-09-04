package uniolunisaar.adam.pnml;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniolunisaar.adam.tools.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static uniolunisaar.adam.pnml.HighLevelSymmetricPetriNetsTests.doAllTests;
import static uniolunisaar.adam.pnml.HighLevelSymmetricPetriNetsTests.parse;

/**
 * These are all the colored nets from the model checking contest 2020.
 */
@Test
public class TestHighLevelRendererAndParserOnMccNets {

    private static final String examplesFolder = System.getProperty("examplesfolder") + "/highlevel/mcc2020/";
    private static final Logger log = Logger.getInstance();

    @BeforeClass
    public void configureLogger() {
        log.setVerbose(false);
        log.setWarningStream(null);
    }

    private static void testNet(String file, String... exclude) throws Exception {
        String pnml = Files.readString(Path.of(examplesFolder, file));
        List<String> excludeList = List.of(exclude);
        doAllTests(parse(pnml, excludeList.contains("-setBased")), pnml, excludeList);
    }

    @Test
    public void testAirplaneLD() throws Exception {
        testNet("AirplaneLD-col-0010.pnml");
    }

    @Test
    public void testBART() throws Exception {
        // isomorphism test very slow (>1h)
        testNet("BART-002.pnml", "-isomorphism");
    }

    @Test
    public void testBridgeAndVehicles() throws Exception {
        // Not set based.
        testNet("BridgeAndVehicles-V04-P05-N02.pnml", "-setBased");
    }

    @Test
    public void testCSRepetitions() throws Exception {
        // Not set based.
        testNet("cs_repetitions-2.pnml", "-setBased");
    }

    @Test
    public void testDatabaseWithMutex() throws Exception {
        testNet("database2.pnml");
    }

    @Test
    public void testDotAndBoxes() throws Exception {
        // -isomorphism: unknown error in generation of low level net.
        testNet("DotAndBoxes2.pnml", "-isomorphism");
    }

    @Test
    public void testDrinkVendingMachine() throws Exception {
        testNet("distributeur-01-SN-02.pnml");
    }

    @Test
    public void testFamilyReunion() throws Exception {
        // isomorphism test very slow (>10m)
        testNet("FamilyReunion-L10-M1-C1-P1-G1.pnml", "-isomorphism");
    }

    @Test
    public void testGlobalResAllocation() throws Exception {
        // Not set based.
        // Unbound
        testNet("galloc_res-3.pnml", "-setBased", "-isomorphism");
    }

    @Test
    public void testLamportFastMutEx() throws Exception {
        testNet("lamport_fmea-2.pnml");
    }

    @Test
    public void testNeoElection() throws Exception {
        testNet("neoelection-2.pnml");
    }

    @Test
    public void testPermAdmissibility() throws Exception {
        // isomorphism test moderately slow (~3m)
        // Not set based.
        testNet("8x8-4stageSEN-02.pnml", "-setBased", "-isomorphism");
    }

    @Test
    public void testPeterson() throws Exception {
        testNet("Peterson-2.pnml");
    }

    @Test
    public void testPhilosophers() throws Exception {
        testNet("philosophers-5.pnml");
    }

    @Test
    public void testPhilosophersDyn() throws Exception {
        testNet("philo_dyn-3.pnml");
    }

    //@Test
    public void testPolyORBLF() throws Exception {
        // Not set based.
        // SetMinus in initial marking
        testNet("PolyORB-LF-S02-J04-T06.pnml", "-setBased");
    }

    @Test
    public void testPolyORBNT() throws Exception {
        // Not set based.
        testNet("PolyORB-NT-S05-J20.pnml", "-setBased");
    }

    @Test
    public void testQuasiCertifProtocol() throws Exception {
        testNet("QCertifProtocol_02.pnml");
    }

    @Test
    public void testReferendum() throws Exception {
        testNet("referendum-10.pnml");
    }

    @Test
    public void testSafeBus() throws Exception {
        testNet("SafeBus-03.pnml");
    }

    @Test
    public void testSharedMemory() throws Exception {
        testNet("SharedMemory-5.pnml");
    }

    @Test
    public void testSudoku() throws Exception {
        testNet("Sudoku-COL-A-N03.pnml");
    }

    @Test
    public void testTokenRing() throws Exception {
        // -isomorphic: unknown error in generation of low level net.
        testNet("TokenRing-5.pnml", "-isomorphism");
    }

    //@Test
    public void testVehicularWifi() throws Exception {
        // initial marking: add inside add inside add ...
        testNet("VehicIEEE80211.pnml");
    }

}
