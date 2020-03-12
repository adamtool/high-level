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
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.petrinet.objectives.Condition;
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
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolver;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverFactory;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

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
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void PD() throws Exception {
//        int nb_drones = 3;
        int nb_drones = 2;
//        int nb_packages = 2;
        int nb_packages = 1;
        HLPetriGame hlgame = PackageDeliveryHL.generateEwithPool(nb_drones, nb_packages, true);
        create("PD" + nb_drones + "_" + nb_packages, hlgame);
    }

    @Test
    public void AS() throws Exception {
        int alarmsystems = 2;
//        int alarmsystems = 1;
        HLPetriGame hlgame = AlarmSystemHL.createSafetyVersionForHLRepWithSetMinus(alarmsystems, true);
        create("AS" + alarmsystems, hlgame);
    }

    @Test
    public void CM() throws Exception {
//        int machines = 4;
        int machines = 2;
//        int orders = 2;
        int orders = 1;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, orders, true);
        create("CM" + machines + "_" + orders, hlgame);
    }

    @Test
    public void DW() throws Exception {
//        int clerks = 4;
        int clerks = 2;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(clerks, true);
        create("DW" + clerks, hlgame);
    }

    @Test
    public void DWs() throws Exception {
//        int clerks = 4;
        int clerks = 2;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
        create("DWs" + clerks, hlgame);
    }

    private void create(String name, HLPetriGame hlgame) throws Exception {
        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + name);
        HLTools.saveHLPG2PDF(outputDir + name, hlgame);

        long timeHLApproach = 0;
        long timeLLApproach = 0;
        long timeExplApproach = 0;
        long timeExplBDDApproach = 0;
        long time, diff;
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HLGraphStrategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% HL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat", stratHL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions());
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeLLApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% HL graph strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat", stratLL);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LLGraphStrategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% LL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat_low", stratHLlow);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeLLApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% LL graph strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat_low", stratLLlow);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        PetriGame pgame = HL2PGConverter.convert(hlgame, true, true);
        // %%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        time = System.currentTimeMillis();
        ExplicitASafetyWithoutType2Solver solverExp = (ExplicitASafetyWithoutType2Solver) ExplicitSolverFactory.getInstance().getSolver(pgame, new ExplicitSolverOptions());
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% LL graph strategy explicit " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "Expl_Gstrat_low", stratExpl);
        // %%%%%%%%%%%%%%%%%%%%%%%%% BDD
        time = System.currentTimeMillis();
        BDDSolverOptions opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        BDDSolver<? extends Condition<?>> solverExplBDD = BDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(pgame, true, false), opt);
        BDDGraph graphBDD = solverExplBDD.getGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplBDDApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% LL graph strategy explicit BDD " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        BDDTools.saveGraph2PDF(outputDir + name + "Expl_BDD_Gstrat_low", graphBDD, solverExplBDD);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Petri game strategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        PetriGame pgStratHL = solverHL.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% Petri game strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "HL_PGstrat", pgStratHL, true);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        PetriGame pgStratLL = solverLL.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeLLApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% Petri game strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "LL_PGstrat", pgStratLL, true);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        // %%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        time = System.currentTimeMillis();
        PetriGame pgStratExpl = solverExp.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% Petri game strategy explicit " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "Expl_PGstrat", pgStratExpl, true);
        // %%%%%%%%%%%%%%%%%%%%%%%%% BDD
        time = System.currentTimeMillis();
        PetriGame pgStratExplBDD = solverExplBDD.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplBDDApproach += diff;
        System.out.println("%%%%%%%%%%%%%%%%% Petri game strategy explicit BDD " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "Expl_BDD_PGstrat", pgStratExplBDD, true);

        time = System.currentTimeMillis();
        boolean winning = solverHL.existsWinningStrategy();
        Assert.assertEquals(winning, solverLL.existsWinningStrategy());
        Assert.assertEquals(winning, solverExp.existsWinningStrategy());
        Assert.assertEquals(winning, solverExplBDD.existsWinningStrategy());
        diff = System.currentTimeMillis() - time;
        System.out.println("All existence took " + Math.round((diff / 1000.0f) * 100.0) / 100.0);

        System.out.println("HLApproach: " + Math.round((timeHLApproach / 1000.0f) * 100.0) / 100.0);
        System.out.println("LLApproach: " + Math.round((timeLLApproach / 1000.0f) * 100.0) / 100.0);
        System.out.println("ExplApproach: " + Math.round((timeExplApproach / 1000.0f) * 100.0) / 100.0);
        System.out.println("ExplBDDApproach: " + Math.round((timeExplBDDApproach / 1000.0f) * 100.0) / 100.0);
    }

}
