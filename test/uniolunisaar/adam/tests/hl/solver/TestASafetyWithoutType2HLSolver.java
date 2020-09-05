package uniolunisaar.adam.tests.hl.solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.hlapproach.HLASafetyWithoutType2SolverHLApproach;
import uniolunisaar.adam.logic.pg.solver.hl.hlapproach.HLSolverFactoryHLApproach;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestASafetyWithoutType2HLSolver {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/sgg/";

    @BeforeClass
    public void logger() {
        Logger.getInstance().setVerbose(false);
    }

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testMachinesHL() throws FileNotFoundException, CalculationInterruptedException, CouldNotFindSuitableConditionException, SolvingException, NotSupportedGameException, ParseException, IOException, InterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM21", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
        HLTools.saveGraph2PDF(outputDir + "CM21_hlgg", graph);
//        HLASafetyWithoutType2Solver<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> solver = new HLASafetyWithoutType2Solver<>();
        HLASafetyWithoutType2SolverHLApproach solver = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        boolean win = solver.isWinning(false);
//        Assert.assertFalse(win);
        Assert.assertTrue(win);

        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> strat = solver.calculateGraphStrategy();
        HLTools.saveGraph2PDF(outputDir + "CM21_hl_strat", strat);

//
//        PetriGame llgame = HL2PGConverter.convert(hlgame, true);
//        llgame = PGTools.getPetriGameFromParsedPetriNet(llgame, true, false);
//        
//        BDDSolverOptions opt = new BDDSolverOptions();
//        opt.setNoType2(true);
//        BDDSolver<? extends Condition> sol = BDDSolverFactory.getInstance().getSolver(llgame, true, opt);
//        boolean winLL = sol.existsWinningStrategy();
//       Assert.assertTrue(winLL);
    }
}
