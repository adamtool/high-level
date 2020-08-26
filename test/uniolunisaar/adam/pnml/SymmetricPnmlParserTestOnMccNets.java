package uniolunisaar.adam.pnml;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * These are all the colored nets from the model checking contest 2020.
 */
public class SymmetricPnmlParserTestOnMccNets {

	private static final String examplesFolder = System.getProperty("examplesfolder") + "/highlevel/mcc2020/";
	private static final String outputDir = System.getProperty("testoutputfolder") + "/hlcreation/";
	private static final Logger log = Logger.getInstance();

	private static final SymmetricPnmlParser parser;
	static {
		SymmetricPnmlParser.Configuration parserConfig = new SymmetricPnmlParser.Configuration();
		parserConfig.checkSetBased = false;
		parser = new SymmetricPnmlParser(parserConfig);
	}
	private static final PnmlRenderer renderer = new PnmlRenderer();

	@BeforeClass
	public void configureLogger() {
		log.setVerbose(false);
		log.setWarningStream(null);
	}

	private static void testNet(String path) throws Exception {
		parseAndRenderRepeatedly(path);
	}

	/**
	 * Just check if the net can be parsed without throwing.
	 */
	private static void parse(String path) throws IOException, ParseException {
		parser.parseFile(examplesFolder + path);
	}

	private static void parseAndRenderRepeatedly(String path) throws IOException, ParseException, RenderException, InterruptedException {
		int iterations = 3;
		List<Integer> lengths = new LinkedList<>();

		HLPetriGame game;
		for (String pnml = Files.readString(Path.of(examplesFolder, path)); lengths.size() < iterations; pnml = renderer.render(game)) {
			lengths.add(pnml.length());
			log.addMessage(pnml);
			game = parser.parseString(pnml);
			HLTools.saveHLPG2PDF(outputDir + game.getName() + "-iteration" + lengths.size(), game, true).join();
		}

		System.out.println((lengths.get(lengths.size() - 2).equals(lengths.get(lengths.size() - 1)) ? "stable" : "unstable") + " " + lengths);
	}

	@Test
	public void testAirplaneLD() throws Exception {
		testNet("AirplaneLD-col-0010.pnml");
	}

	@Test
	public void testBART() throws Exception {
		testNet("BART-002.pnml");
	}

	@Test
	public void testBridgeAndVehicles() throws Exception {
		// Not set based.
		testNet("BridgeAndVehicles-V04-P05-N02.pnml");
	}

	@Test
	public void testCSRepetitions() throws Exception {
		// Not set based.
		testNet("cs_repetitions-2.pnml");
	}

	@Test
	public void testDatabaseWithMutex() throws Exception {
		testNet("database2.pnml");
	}

	@Test
	public void testDotAndBoxes() throws Exception {
		testNet("DotAndBoxes2.pnml");
	}

	@Test
	public void testDrinkVendingMachine() throws Exception {
		testNet("distributeur-01-SN-02.pnml");
	}

	@Test
	public void testFamilyReunion() throws Exception {
		testNet("FamilyReunion-L10-M1-C1-P1-G1.pnml");
	}

	@Test
	public void testGlobalResAllocation() throws Exception {
		// Not set based.
		testNet("galloc_res-3.pnml");
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
		// Not set based.
		testNet("8x8-4stageSEN-02.pnml");
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
		testNet("PolyORB-LF-S02-J04-T06.pnml");
	}

	@Test
	public void testPolyORBNT() throws Exception {
		// Not set based.
		testNet("PolyORB-NT-S05-J20.pnml");
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
		testNet("TokenRing-5.pnml");
	}

	//@Test
	public void testVehicularWifi() throws Exception {
		// initial marking: add inside add inside add ...
		testNet("VehicIEEE80211.pnml");
	}

}