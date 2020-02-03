package uniolunisaar.adam.tests.hl.solver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.io.parser.ParseException;
import uniolunisaar.adam.ds.graph.hl.SGG;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.IHLDecision;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.solver.SGGASafetyWithoutType2Solver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestASafetyWithoutType2HLSolver {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/sgg/";

    @BeforeClass
    public void logger() {
        Logger.getInstance().setVerbose(true);
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
        SGG<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
        HLTools.saveGraph2PDF(outputDir+"CM21_hlgg", graph);
        SGGASafetyWithoutType2Solver<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> solver = new SGGASafetyWithoutType2Solver<>();
        boolean win = solver.isWinning(graph, false);
//        Assert.assertFalse(win);
        Assert.assertTrue(win);

        SGG<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> strat = solver.calculateGraphStrategy(graph, false);
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
