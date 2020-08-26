package uniolunisaar.adam.pnml;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.hl.ContainerHabourHL;
import uniolunisaar.adam.generators.hl.DocumentWorkflowHL;
import uniolunisaar.adam.generators.hl.PackageDeliveryHL;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;

import java.io.FileNotFoundException;

public class PnmlRendererTest {

	private static final String outputDir = System.getProperty("testoutputfolder") + "/hlcreation/";

	@BeforeClass
	public void configureLogger() {
		Logger.getInstance().setVerbose(true);
	}

	/**
	 * Render and parse again to see (with human eyes) if that works
	 */
	private void testGame(HLPetriGame game) throws RenderException, ParseException, FileNotFoundException {
		PnmlRenderer renderer = new PnmlRenderer();
		SymmetricPnmlParser parser = new SymmetricPnmlParser();

		String renderedGame = renderer.render(game);
		System.out.println(renderedGame);
		HLPetriGame parsedGame = parser.parseString(renderedGame);
		HLTools.saveHLPG2PDF(outputDir + parsedGame.getName(), game, true);
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
		testGame(ContainerHabourHL.generateD(4, 2, 1, 1, true));
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
