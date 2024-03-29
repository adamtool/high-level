package uniolunisaar.adam.tests.synthesis.hl;

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
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphUsingIDs;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.exceptions.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.SolvingException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.generators.highlevel.AlarmSystemHL;
import uniolunisaar.adam.generators.highlevel.ConcurrentMachinesHL;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.explicit.GGBuilder;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.membership.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.hlapproach.HLASafetyWithoutType2SolverHLApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.hlapproach.HLSolverFactoryHLApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.llapproach.HLASafetyWithoutType2SolverLLApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.llapproach.HLSolverFactoryLLApproach;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestGGvsSGG {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/gg_vs_sgg/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(true);
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
//        comparePGStrategies("CM" + machines + "_" + orders, hlgame);
    }

    private void compareGraphs(String name, HLPetriGame hlgame) throws FileNotFoundException, CalculationInterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, IOException, InterruptedException {
        HLTools.saveHLPG2PDF(outputDir + name, hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        long time, diff;
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
//        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
//        GameGraphUsingIDs<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
        AbstractGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("Size HL: " + graph.getStatesView().size() + "(time " + diff / 1000 + ")");
//        HLTools.saveGraph2PDF(outputDir + name + "HL_sgg", graph);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graphll = SGGBuilderLL.getInstance().create(hlgame);
//        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graphll = SGGBuilderLL.getInstance().create(hlgame);
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graphll = SGGBuilderLL.getInstance().create(hlgame);
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("Size LL: " + graphll.getStatesView().size() + "(time " + diff / 1000 + ")");
//        HLTools.saveGraph2PDF(outputDir + name + "LL_sgg", graphll);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% BDD APPROACH
        time = System.currentTimeMillis();
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true, true);
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        BDDSolverOptions opt = new BDDSolverOptions(true);
        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(new DistrSysBDDSolvingObject<>(pgame, new Safety()), syms, opt);
        sol.initialize();
        BDD states = sol.getBufferedDCSs();
        double sizeBDD = states.satCount(sol.getFirstBDDVariables()) + 1;
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("Size BDD: " + sizeBDD + "(time " + diff / 1000 + ")");

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT      
        time = System.currentTimeMillis();
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> explicitGraph = GGBuilder.getInstance().create(pgame);
        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> explicitGraph = GGBuilder.getInstance().create(pgame);
        diff = System.currentTimeMillis() - time;
//        HLTools.saveGraph2PDF(outputDir + name + "explicit_gg", explicitGraph);
        Logger.getInstance().addMessage("Size explicit: " + explicitGraph.getStatesView().size() + "(time " + diff / 1000 + ")");
    }

    private void compareExWinStrat(HLPetriGame hlgame, boolean exists) throws Exception, CouldNotFindSuitableConditionException, SolvingException, NotSupportedGameException, NoSuitableDistributionFoundException, NetNotSafeException {
        long time, diff;
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
//        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        boolean hl = solverHL.existsWinningStrategy();
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("HL ex strat " + "(time " + diff / 1000 + ")");
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
//        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        boolean ll = solverLL.existsWinningStrategy();
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("LL ex strat " + "(time " + diff / 1000 + ")");
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% BDD APPROACH
        time = System.currentTimeMillis();
//        HLASafetyWithoutType2SolverBDDApproach solverBDD = (HLASafetyWithoutType2SolverBDDApproach) HLSolverFactoryBDDApproach.getInstance().getSolver(hlgame, new BDDSolverOptions(true));
//        boolean bdd = solverBDD.existsWinningStrategy();
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("HLBDD ex strat " + "(time " + diff / 1000 + ")");

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true, true);
        time = System.currentTimeMillis();
        ExplicitASafetyWithoutType2Solver solverExp = (ExplicitASafetyWithoutType2Solver) ExplicitSolverFactory.getInstance().getSolver(pgame, new ExplicitSolverOptions());
        boolean expl = solverExp.existsWinningStrategy();
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("Expl ex strat " + "(time " + diff / 1000 + ")");

        Assert.assertEquals(hl, exists, "HL");
        Assert.assertEquals(ll, exists, "LL");
//        Assert.assertEquals(bdd, exists, "BDD");
        Assert.assertEquals(expl, exists, "expl");
    }

    private void compareGraphStrategies(String name, HLPetriGame hlgame) throws Exception {
        HLTools.saveHLPG2PDF(outputDir + name + "PG", hlgame);

        long time, diff;
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
//        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
//        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
//        GameGraphUsingIDs<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
        AbstractGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat", stratHL);
//        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
//        GameGraphUsingIDs<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
        AbstractGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat_low", stratHLlow);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
//        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
//        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat", stratLL);
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
//        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat_low", stratLLlow);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% BDD APPROACH
//        HLASafetyWithoutType2SolverBDDApproach solverBDD = (HLASafetyWithoutType2SolverBDDApproach) HLSolverFactoryBDDApproach.getInstance().getSolver(hlgame, new BDDSolverOptions(true));
//        BDDGraph stratBDD = solverBDD.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true, true);
        time = System.currentTimeMillis();
        ExplicitASafetyWithoutType2Solver solverExp = (ExplicitASafetyWithoutType2Solver) ExplicitSolverFactory.getInstance().getSolver(pgame, new ExplicitSolverOptions());
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
//        GameGraphUsingIDs<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
        AbstractGameGraph<Place, Transition, ILLDecision, DecisionSet, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        HLTools.saveGraph2PDF(outputDir + name + "Expl_Gstrat", stratExpl);
    }

    private void comparePGStrategies(String name, HLPetriGame hlgame) throws Exception {
        long time, diff;
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
////        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
//        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
//        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
//        HLTools.saveGraph2PDF(outputDir + name + "HL_strat", stratHL);
//        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
////        HLSolver<? extends Condition<?>, ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> solver = HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
//        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
//        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
//        HLTools.saveGraph2PDF(outputDir + name + "LL_strat", stratLL);
//        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% BDD APPROACH
////        HLASafetyWithoutType2SolverBDDApproach solverBDD = (HLASafetyWithoutType2SolverBDDApproach) HLSolverFactoryBDDApproach.getInstance().getSolver(hlgame, new BDDSolverOptions(true));
////        BDDGraph stratBDD = solverBDD.calculateGraphStrategy();

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true, true);
        time = System.currentTimeMillis();
        ExplicitASafetyWithoutType2Solver solverExp = (ExplicitASafetyWithoutType2Solver) ExplicitSolverFactory.getInstance().getSolver(pgame, new ExplicitSolverOptions());
        PetriGameWithTransits stratExpl = solverExp.getStrategy();
        diff = System.currentTimeMillis() - time;
        PGTools.savePG2PDF(outputDir + name + "Expl_PGstrat", stratExpl, true);
    }
}
