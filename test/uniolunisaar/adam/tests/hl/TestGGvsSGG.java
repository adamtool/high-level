package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.sf.javabdd.BDD;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.graph.GameGraph;
import uniolunisaar.adam.ds.graph.GameGraphFlow;
import uniolunisaar.adam.ds.graph.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.logic.pg.builder.graph.explicit.GGBuilder;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.pg.solver.explicit.ExplicitASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.pg.solver.explicit.ExplicitSolverFactory;
import uniolunisaar.adam.logic.pg.solver.explicit.ExplicitSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.bddapproach.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.logic.pg.solver.hl.bddapproach.HLASafetyWithoutType2SolverBDDApproach;
import uniolunisaar.adam.logic.pg.solver.hl.bddapproach.HLSolverFactoryBDDApproach;
import uniolunisaar.adam.logic.pg.solver.hl.hlapproach.HLASafetyWithoutType2SolverHLApproach;
import uniolunisaar.adam.logic.pg.solver.hl.hlapproach.HLSolverFactoryHLApproach;
import uniolunisaar.adam.logic.pg.solver.hl.llapproach.HLASafetyWithoutType2SolverLLApproach;
import uniolunisaar.adam.logic.pg.solver.hl.llapproach.HLSolverFactoryLLApproach;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolvingObject;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestGGvsSGG {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/gg_vs_sgg/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(true);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void alarmSystem() throws ModuleException, FileNotFoundException, NotSupportedGameException, NetNotSafeException, InvalidPartitionException, NoSuitableDistributionFoundException, CalculationInterruptedException, InterruptedException, IOException {
        int alarmsystems = 2;
        HLPetriGame hlgame = AlarmSystemHL.createSafetyVersionForHLRepWithSetMinus(alarmsystems, true);
        compareGraphs("AS" + alarmsystems, hlgame);
    }

    @Test
    public void CM() throws ModuleException, FileNotFoundException, NotSupportedGameException, NetNotSafeException, InvalidPartitionException, NoSuitableDistributionFoundException, CalculationInterruptedException, InterruptedException, IOException, CouldNotFindSuitableConditionException, SolvingException, Exception {
        int machines = 2;
        int orders = 1;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, orders, true);
//        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersion(machines, orders, true);
//        compareGraphs("CM" + machines + "_" + orders, hlgame);
//        compareExWinStrat(hlgame, false);
        compareGraphStrategies("CM" + machines + "_" + orders, hlgame);
    }

    private void compareGraphs(String name, HLPetriGame hlgame) throws FileNotFoundException, CalculationInterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, IOException, InterruptedException {
        HLTools.saveHLPG2PDF(outputDir + name, hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
//        HLTools.saveGraph2PDF(outputDir + name + "HL_sgg", graph);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graphll = SGGBuilderLL.getInstance().create(hlgame);
//        HLTools.saveGraph2PDF(outputDir + name + "LL_sgg", graphll);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% BDD APPROACH
        PetriGame pgame = HL2PGConverter.convert(hlgame, true, true);
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        BDDSolverOptions opt = new BDDSolverOptions(true);
        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(new BDDSolvingObject<>(pgame, new Safety()), syms, opt);
        sol.initialize();
        BDD states = sol.getBufferedDCSs();
        double sizeBDD = states.satCount(sol.getFirstBDDVariables()) + 1;

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT      
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> explicitGraph = GGBuilder.getInstance().create(pgame);
//        HLTools.saveGraph2PDF(outputDir + name + "explicit_gg", explicitGraph);

        System.out.println("Size HL: " + graph.getStatesView().size());
        System.out.println("Size LL: " + graphll.getStatesView().size());
        System.out.println("Size BDD: " + sizeBDD);
        System.out.println("Size explicit: " + explicitGraph.getStatesView().size());
    }

    private void compareExWinStrat(HLPetriGame hlgame, boolean exists) throws Exception, CouldNotFindSuitableConditionException, SolvingException, NotSupportedGameException, NoSuitableDistributionFoundException, NetNotSafeException {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
//        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        boolean hl = solverHL.existsWinningStrategy();
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
//        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        boolean ll = solverLL.existsWinningStrategy();
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% BDD APPROACH
//        HLASafetyWithoutType2SolverBDDApproach solverBDD = (HLASafetyWithoutType2SolverBDDApproach) HLSolverFactoryBDDApproach.getInstance().getSolver(hlgame, new BDDSolverOptions(true));
//        boolean bdd = solverBDD.existsWinningStrategy();

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        PetriGame pgame = HL2PGConverter.convert(hlgame, true, true);
        ExplicitASafetyWithoutType2Solver solverExp = (ExplicitASafetyWithoutType2Solver) ExplicitSolverFactory.getInstance().getSolver(pgame, new ExplicitSolverOptions());
        boolean expl = solverExp.existsWinningStrategy();

        Assert.assertEquals(hl, exists, "HL");
        Assert.assertEquals(ll, exists, "LL");
//        Assert.assertEquals(bdd, exists, "BDD");
        Assert.assertEquals(expl, exists, "expl");
    }

    private void compareGraphStrategies(String name, HLPetriGame hlgame) throws Exception {
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
//        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
        HLTools.saveGraph2PDF(outputDir + name + "HL_strat", stratHL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
//        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        HLTools.saveGraph2PDF(outputDir + name + "LL_strat", stratLL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% BDD APPROACH
//        HLASafetyWithoutType2SolverBDDApproach solverBDD = (HLASafetyWithoutType2SolverBDDApproach) HLSolverFactoryBDDApproach.getInstance().getSolver(hlgame, new BDDSolverOptions(true));
//        BDDGraph stratBDD = solverBDD.calculateGraphStrategy();

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        PetriGame pgame = HL2PGConverter.convert(hlgame, true, true);
        ExplicitASafetyWithoutType2Solver solverExp = (ExplicitASafetyWithoutType2Solver) ExplicitSolverFactory.getInstance().getSolver(pgame, new ExplicitSolverOptions());
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
        HLTools.saveGraph2PDF(outputDir + name + "Expl_strat", stratExpl);
    }
}
