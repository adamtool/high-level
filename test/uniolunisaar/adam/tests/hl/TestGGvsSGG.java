package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import net.sf.javabdd.BDD;
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
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.logic.pg.builder.graph.explicit.GGBuilder;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.pg.builder.graph.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.pg.solver.hl.bddapproach.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolvingObject;
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
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void alarmSystem() throws ModuleException, FileNotFoundException, NotSupportedGameException, NetNotSafeException, InvalidPartitionException, NoSuitableDistributionFoundException, CalculationInterruptedException, InterruptedException, IOException {
        int alarmsystems = 2;
        HLPetriGame hlgame = AlarmSystemHL.createSafetyVersionForHLRepWithSetMinus(alarmsystems, true);
        compare("AS" + alarmsystems, hlgame);
    }

    @Test
    public void CM() throws ModuleException, FileNotFoundException, NotSupportedGameException, NetNotSafeException, InvalidPartitionException, NoSuitableDistributionFoundException, CalculationInterruptedException, InterruptedException, IOException {
        int machines = 2;
        int orders = 2;
//        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, orders, true);
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersion(machines, orders, true);
        compare("CM" + machines + "_" + orders, hlgame);
    }

    private void compare(String name, HLPetriGame hlgame) throws FileNotFoundException, CalculationInterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, IOException, InterruptedException {
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

}
