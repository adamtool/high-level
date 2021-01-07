package uniolunisaar.adam.tests.synthesis.hl;

import java.io.File;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.generators.highlevel.ConcurrentMachinesHL;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.logic.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitASafetyWithoutType2Solver;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitSolverFactory;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.explicit.ExplicitSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.HLSolverOptions;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.hlapproach.HLASafetyWithoutType2SolverHLApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.hlapproach.HLSolverFactoryHLApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.llapproach.HLASafetyWithoutType2SolverLLApproach;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.llapproach.HLSolverFactoryLLApproach;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestCanonicalRepVsMembershipBDDApproach {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/sgg/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testCM() throws Exception {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(2, 1, true);
        create("CM21", hlgame);
    }

    private void create(String name, HLPetriGame hlgame) throws Exception {
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% " + name);
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
        HLASafetyWithoutType2SolverHLApproach solverHL = (HLASafetyWithoutType2SolverHLApproach) HLSolverFactoryHLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHL = solverHL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat", stratHL);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        HLASafetyWithoutType2SolverLLApproach solverLL = (HLASafetyWithoutType2SolverLLApproach) HLSolverFactoryLLApproach.getInstance().getSolver(hlgame, new HLSolverOptions(true));
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLL = solverLL.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeLLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% HL graph strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat", stratLL);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LLGraphStrategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> stratHLlow = solverHL.calculateLLGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "HL_Gstrat_low", stratHLlow);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratLLlow = solverLL.calculateLLGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeLLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "LL_Gstrat_low", stratLLlow);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true, true);
        // %%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        time = System.currentTimeMillis();
        ExplicitASafetyWithoutType2Solver solverExp = (ExplicitASafetyWithoutType2Solver) ExplicitSolverFactory.getInstance().getSolver(pgame, new ExplicitSolverOptions());
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> stratExpl = solverExp.calculateGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy explicit " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        HLTools.saveGraph2PDF(outputDir + name + "Expl_Gstrat_low", stratExpl);
        // %%%%%%%%%%%%%%%%%%%%%%%%% BDD
        time = System.currentTimeMillis();
        BDDSolverOptions opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        DistrSysBDDSolver<? extends Condition<?>> solverExplBDD = DistrSysBDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(pgame, true, false), opt);
        BDDGraph graphBDD = solverExplBDD.getGraphStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplBDDApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% LL graph strategy explicit BDD " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        BDDTools.saveGraph2PDF(outputDir + name + "Expl_BDD_Gstrat_low", graphBDD, solverExplBDD);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Petri game strategy
        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% SYMBOLIC
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% HIGH LEVEL
        time = System.currentTimeMillis();
        PetriGameWithTransits pgStratHL = solverHL.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeHLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% Petri game strategy HL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "HL_PGstrat", pgStratHL, true);
        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        time = System.currentTimeMillis();
        PetriGameWithTransits pgStratLL = solverLL.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeLLApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% Petri game strategy LL approach " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "LL_PGstrat", pgStratLL, true);

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        // %%%%%%%%%%%%%%%%%%%%%%%%% EXPLICIT
        time = System.currentTimeMillis();
        PetriGameWithTransits pgStratExpl = solverExp.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% Petri game strategy explicit " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "Expl_PGstrat", pgStratExpl, true);
        // %%%%%%%%%%%%%%%%%%%%%%%%% BDD
        time = System.currentTimeMillis();
        PetriGameWithTransits pgStratExplBDD = solverExplBDD.getStrategy();
        diff = System.currentTimeMillis() - time;
        timeExplBDDApproach += diff;
        Logger.getInstance().addMessage("%%%%%%%%%%%%%%%%% Petri game strategy explicit BDD " + Math.round((diff / 1000.0f) * 100.0) / 100.0);
        PGTools.savePG2PDF(outputDir + name + "Expl_BDD_PGstrat", pgStratExplBDD, true);

        time = System.currentTimeMillis();
        boolean winning = solverHL.existsWinningStrategy();
        Assert.assertEquals(winning, solverLL.existsWinningStrategy());
        Assert.assertEquals(winning, solverExp.existsWinningStrategy());
        Assert.assertEquals(winning, solverExplBDD.existsWinningStrategy());
        diff = System.currentTimeMillis() - time;
        Logger.getInstance().addMessage("All existence took " + Math.round((diff / 1000.0f) * 100.0) / 100.0);

        Logger.getInstance().addMessage("HLApproach: " + Math.round((timeHLApproach / 1000.0f) * 100.0) / 100.0);
        Logger.getInstance().addMessage("LLApproach: " + Math.round((timeLLApproach / 1000.0f) * 100.0) / 100.0);
        Logger.getInstance().addMessage("ExplApproach: " + Math.round((timeExplApproach / 1000.0f) * 100.0) / 100.0);
        Logger.getInstance().addMessage("ExplBDDApproach: " + Math.round((timeExplBDDApproach / 1000.0f) * 100.0) / 100.0);
    }
}
