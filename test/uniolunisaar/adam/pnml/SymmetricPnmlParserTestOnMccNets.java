package uniolunisaar.adam.pnml;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.asserts.Assertion;
import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
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
	private static final Assertion TEST = new Assertion();

	@BeforeClass
	public void configureLogger() {
		log.setVerbose(false);
		log.setWarningStream(null);
	}

	private static void common(String path) throws Exception {
		System.out.println(path);
		//parse(path);
		//renderPdf(path);
		TEST.assertTrue(parseAndRenderRepeatedly(path));
		TEST.assertTrue(checkIsomorphicAfterParseRenderParse(path));
	}

	/**
	 * Just check if the net can be parsed without throwing.
	 */
	private static HLPetriGame parse(String path) throws IOException, ParseException {
		return parser.parseFile(examplesFolder + path);
	}

	private static void renderPdf(String path) throws IOException, ParseException, InterruptedException {
		HLPetriGame game = parse(path);
		HLTools.saveHLPG2PDF(outputDir + game.getName(), game, true).join();
	}

	private static boolean parseAndRenderRepeatedly(String path) throws IOException, ParseException, RenderException, InterruptedException, UnboundedException {
		int iterations = 3;
		List<Integer> lengths = new LinkedList<>();

		HLPetriGame game;
		for (String pnml = Files.readString(Path.of(examplesFolder, path)); lengths.size() < iterations; pnml = renderer.render(game)) {
			lengths.add(pnml.length());
			log.addMessage(pnml);
			game = parser.parseString(pnml);
			HLTools.saveHLPG2PDF(outputDir + game.getName() + "-iteration" + lengths.size(), game, true).join();
		}

		boolean stable = lengths.get(lengths.size() - 2).equals(lengths.get(lengths.size() - 1));
		System.out.println((stable ? "stable" : "unstable") + " " + lengths);
		return stable;
	}

	private static boolean isIsomorphic(HLPetriGame game1, HLPetriGame game2) throws UnboundedException {
		IsomorphismLogic isomorphismLogic = new IsomorphismLogic(
				CoverabilityGraph.get(HL2PGConverter.convert(game1, true, true)).toReachabilityLTS(),
				CoverabilityGraph.get(HL2PGConverter.convert(game2, true, true)).toReachabilityLTS(),
				true
		);

		return isomorphismLogic.isIsomorphic();
	}

	private static boolean checkIsomorphicAfterParseRenderParse(String path) throws IOException, ParseException, RenderException, UnboundedException {
		HLPetriGame game1 = parse(path);
		HLPetriGame game2 = parser.parseString(renderer.render(parser.parseString(renderer.render(game1))));
		boolean isomorphic = isIsomorphic(game1, game2);
		System.out.println("p(f) " + (isomorphic ? "isomorphic" : "NOT isomorphic") + " p(r(p(r(p(f)))))");
		return isomorphic;
	}

	@Test
	public void testAirplaneLD() throws Exception {
		common("AirplaneLD-col-0010.pnml");
	}

	//@Test
	public void testBART() throws Exception {
		// isomorphism test very slow (>1h)
		common("BART-002.pnml");
	}

	@Test
	public void testBridgeAndVehicles() throws Exception {
		// Not set based.
		common("BridgeAndVehicles-V04-P05-N02.pnml");
	}

	@Test
	public void testCSRepetitions() throws Exception {
		// Not set based.
		common("cs_repetitions-2.pnml");
	}

	@Test
	public void testDatabaseWithMutex() throws Exception {
		common("database2.pnml");
	}

	@Test
	public void testDotAndBoxes() throws Exception {
		common("DotAndBoxes2.pnml");
	}

	@Test
	public void testDrinkVendingMachine() throws Exception {
		common("distributeur-01-SN-02.pnml");
	}

	//@Test
	public void testFamilyReunion() throws Exception {
		// isomorphism test very slow (>10m)
		common("FamilyReunion-L10-M1-C1-P1-G1.pnml");
	}

	@Test
	public void testGlobalResAllocation() throws Exception {
		// Not set based.
		common("galloc_res-3.pnml");
	}

	@Test
	public void testLamportFastMutEx() throws Exception {
		common("lamport_fmea-2.pnml");
	}

	@Test
	public void testNeoElection() throws Exception {
		common("neoelection-2.pnml");
	}

	//@Test
	public void testPermAdmissibility() throws Exception {
		// isomorphism test moderately slow (~1m)
		// Not set based.
		common("8x8-4stageSEN-02.pnml");
	}

	@Test
	public void testPeterson() throws Exception {
		common("Peterson-2.pnml");
	}

	@Test
	public void testPhilosophers() throws Exception {
		common("philosophers-5.pnml");
	}

	@Test
	public void testPhilosophersDyn() throws Exception {
		common("philo_dyn-3.pnml");
	}

	//@Test
	public void testPolyORBLF() throws Exception {
		// Not set based.
		// SetMinus in initial marking
		common("PolyORB-LF-S02-J04-T06.pnml");
	}

	@Test
	public void testPolyORBNT() throws Exception {
		// Not set based.
		common("PolyORB-NT-S05-J20.pnml");
	}

	@Test
	public void testQuasiCertifProtocol() throws Exception {
		common("QCertifProtocol_02.pnml");
	}

	@Test
	public void testReferendum() throws Exception {
		common("referendum-10.pnml");
	}

	@Test
	public void testSafeBus() throws Exception {
		common("SafeBus-03.pnml");
	}

	@Test
	public void testSharedMemory() throws Exception {
		common("SharedMemory-5.pnml");
	}

	@Test
	public void testSudoku() throws Exception {
		common("Sudoku-COL-A-N03.pnml");
	}

	@Test
	public void testTokenRing() throws Exception {
		common("TokenRing-5.pnml");
	}

	//@Test
	public void testVehicularWifi() throws Exception {
		// initial marking: add inside add inside add ...
		common("VehicIEEE80211.pnml");
	}

}