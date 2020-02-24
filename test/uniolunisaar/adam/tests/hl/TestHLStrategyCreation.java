package uniolunisaar.adam.tests.hl;

import java.io.File;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
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
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.hl.DocumentWorkflowHL;
import uniolunisaar.adam.generators.hl.PackageDeliveryHL;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.pg.solver.explicit.ExplicitASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.pg.solver.explicit.ExplicitSolverFactory;
import uniolunisaar.adam.logic.pg.solver.explicit.ExplicitSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.HLSolverOptions;
import uniolunisaar.adam.logic.pg.solver.hl.hlapproach.HLASafetyWithoutType2SolverHLApproach;
import uniolunisaar.adam.logic.pg.solver.hl.hlapproach.HLSolverFactoryHLApproach;
import uniolunisaar.adam.logic.pg.solver.hl.llapproach.HLASafetyWithoutType2SolverLLApproach;
import uniolunisaar.adam.logic.pg.solver.hl.llapproach.HLSolverFactoryLLApproach;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestHLStrategyCreation {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/hlstratcreation/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(true);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void PG() throws Exception {
        int nb_drones = 2;
        int nb_packages = 2;
        HLPetriGame hlgame = PackageDeliveryHL.generateEwithPool(nb_drones, nb_packages, true);
        create("PG" + nb_drones + "_" + nb_packages, hlgame);
    }

    @Test
    public void AS() throws Exception {
        int alarmsystems = 2;
        HLPetriGame hlgame = AlarmSystemHL.createSafetyVersionForHLRepWithSetMinus(alarmsystems, true);
        create("AS" + alarmsystems, hlgame);
    }

    @Test
    public void CM() throws Exception {
        int machines = 3;
        int orders = 2;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, orders, true);
        create("CM" + machines + "_" + orders, hlgame);
    }

    @Test
    public void DW() throws Exception {
        int clerks = 3;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(clerks, true);
        create("DW" + clerks, hlgame);
    }

    @Test
    public void DWs() throws Exception {
        int clerks = 1;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
        create("DWs" + clerks, hlgame);
    }

    private void create(String name, HLPetriGame hlgame) throws Exception {
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + name);
        HLTools.saveHLPG2PDF(outputDir + name, hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        long time, diff;
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HLGraphStrategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        System.out.println("%%%%%%%%%%%%%%%%% HL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat", stratHL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        System.out.println("%%%%%%%%%%%%%%%%% HL graph strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat", stratLL);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LLGraphStrategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
        diff = System.currentTimeMillis() - time;
        System.out.println("%%%%%%%%%%%%%%%%% LL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat_low", stratHLlow);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
        diff = System.currentTimeMillis() - time;
        System.out.println("%%%%%%%%%%%%%%%%% LL graph strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat_low", stratLLlow);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        PetriGame pgame = HL2PGConverter.convert(hlgame, true, true);
        time = System.currentTimeMillis();
        ExplicitASafetyWithoutType2Solver solverExp = (ExplicitASafetyWithoutType2Solver) ExplicitSolverFactory.getInstance().getSolver(pgame, new ExplicitSolverOptions());
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        System.out.println("%%%%%%%%%%%%%%%%% LL graph strategy explicit " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "Expl_Gstrat_low", stratExpl);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Petri game strategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        PetriGame pgStratHL = solverHL.getStrategy();
        diff = System.currentTimeMillis() - time;
        System.out.println("%%%%%%%%%%%%%%%%% Petri game strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "HL_PGstrat", pgStratHL, true);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        PetriGame pgStratLL = solverLL.getStrategy();
        diff = System.currentTimeMillis() - time;
        System.out.println("%%%%%%%%%%%%%%%%% Petri game strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "LL_PGstrat", pgStratLL, true);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        time = System.currentTimeMillis();
        PetriGame pgStratExpl = solverExp.getStrategy();
        diff = System.currentTimeMillis() - time;
        System.out.println("%%%%%%%%%%%%%%%%% Petri game strategy explicit " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "Expl_PGstrat", pgStratExpl, true);

        time = System.currentTimeMillis();
        boolean winning = solverHL.existsWinningStrategy();
        Assert.assertEquals(winning, solverLL.existsWinningStrategy());
        Assert.assertEquals(winning, solverExp.existsWinningStrategy());
        diff = System.currentTimeMillis() - time;
        System.out.println("All existence took " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
    }

}
