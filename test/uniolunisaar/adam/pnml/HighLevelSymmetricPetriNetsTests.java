package uniolunisaar.adam.pnml;

import uniol.apt.analysis.coverability.CoverabilityGraph;
import uniol.apt.analysis.exception.UnboundedException;
import uniol.apt.analysis.isomorphism.IsomorphismLogic;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
import uniolunisaar.adam.tools.Logger;

import java.util.*;

import static org.testng.Assert.assertTrue;

public class HighLevelSymmetricPetriNetsTests {

	public static int MAXIMUM_ITERATIONS_FOR_STABILISATION = 3;

	private static final Logger log = Logger.getInstance();

	private static final SymmetricPnmlParser.Configuration parserConfig = new SymmetricPnmlParser.Configuration();
	private static final SymmetricPnmlParser parser = new SymmetricPnmlParser(parserConfig);

	private static final PnmlRenderer renderer = new PnmlRenderer();

	public static HLPetriGame parse(String pnml, boolean ignoreSetBased) throws ParseException {
		try {
			parserConfig.checkSetBased = !ignoreSetBased;
			return parser.parseString(pnml);
		} finally {
			parserConfig.checkSetBased = true;
		}
	}

	public static String render(HLPetriGame game) throws RenderException {
		return renderer.render(game);
	}

	public static void doAllTests(HLPetriGame game, String pnml, List<String> exclude) throws ParseException, RenderException, UnboundedException {
		try {
			if (exclude.contains("-setBased")) {
				parserConfig.checkSetBased = false;
			}
			if (!exclude.contains("-stableLength")) {
				assertTrue(doesPnmlLengthStabilize(pnml));
			}
			if (!exclude.contains("-stable")) {
				assertTrue(doesPnmlStabilize(pnml));
			}
			if (!exclude.contains("-isomorphism")) {
				assertTrue(isIsomorphicAfterOneRenderParseCycle(game));
				assertTrue(isIsomorphicAfterTwoRenderParseCycles(game));
			}
		} finally {
			parserConfig.checkSetBased = true;
		}
	}

	private static boolean doesPnmlLengthStabilize(String pnml) throws ParseException, RenderException {
		System.out.println("begin test doesPnmlLengthStabilize");
		for (int i = 0; i < MAXIMUM_ITERATIONS_FOR_STABILISATION; i++) {
			log.addMessage(pnml);
			int lastLength = pnml.length();
			pnml = renderer.render(parser.parseString(pnml));
			if (pnml.length() == lastLength) {
				System.out.println("pnml stabilised after rendering " + (i + 1) + " times");
				return true;
			}
		}
		return false;
	}

	private static boolean doesPnmlStabilize(String pnml) throws ParseException, RenderException {
		System.out.println("begin test doesPnmlStabilize");
		for (int i = 0; i < MAXIMUM_ITERATIONS_FOR_STABILISATION; i++) {
			log.addMessage(pnml);
			String lastPnml = pnml;
			pnml = renderer.render(parser.parseString(pnml));
			if (pnml.equals(lastPnml)) {
				System.out.println("pnml stabilised after rendering " + (i + 1) + " times");
				return true;
			}
		}
		return false;
	}

	private static boolean isIsomorphicAfterOneRenderParseCycle(HLPetriGame game) throws RenderException, ParseException, UnboundedException {
		System.out.println("begin test isIsomorphicAfterOneRenderParseCycle");
		return isIsomorphic(game, parser.parseString(renderer.render(game)));
	}

	private static boolean isIsomorphicAfterTwoRenderParseCycles(HLPetriGame game) throws RenderException, ParseException, UnboundedException {
		System.out.println("begin test isIsomorphicAfterTwoRenderParseCycles");
		return isIsomorphic(game, parser.parseString(renderer.render(parser.parseString(renderer.render(game)))));
	}

	private static boolean isIsomorphic(HLPetriGame game1, HLPetriGame game2) throws UnboundedException {
		IsomorphismLogic isomorphismLogic = new IsomorphismLogic(
				CoverabilityGraph.get(toLowLevel(game1)).toReachabilityLTS(),
				CoverabilityGraph.get(toLowLevel(game2)).toReachabilityLTS(),
				true
		);

		return isomorphismLogic.isIsomorphic();
	}

	private static PetriGame toLowLevel(HLPetriGame highLevel) {
		return HL2PGConverter.convert(highLevel, true, true);
	}
}
