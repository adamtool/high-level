package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import net.sf.javabdd.BDD;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.module.exception.ModuleException;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractGameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IntegerID;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraph;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphFlow;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.GameGraphByHashCode;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.HLDecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach.IHLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.ValuationIterator;
import uniolunisaar.adam.ds.synthesis.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.objectives.local.Safety;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.exceptions.synthesis.pgwt.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.synthesis.pgwt.NotSupportedGameException;
import uniolunisaar.adam.generators.highlevel.AlarmSystemHL;
import uniolunisaar.adam.generators.highlevel.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.highlevel.DocumentWorkflowHL;
import uniolunisaar.adam.generators.highlevel.PackageDeliveryHL;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.synthesis.builder.twoplayergame.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.synthesis.solver.twoplayergame.hl.bddapproach.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.ds.synthesis.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.util.HLTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class CompareApproaches {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/compare/";

    @BeforeClass
    public void createFolder() {
        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void testDW() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(3, true);
        HLTools.saveHLPG2PDF(outputDir + "DWs1HL", hlgame);
        // test ndet 
//        Valuation val = new Valuation();
//        val.put(new Variable("x"), new Color("c0"));
//        Set<IHLDecision> decisions = new HashSet<>();
//        decisions.add(new HLEnvDecision(hlgame.getPlace("start"), new ColorToken(new Color("e"))));
//        decisions.add(new HLSysDecision(hlgame.getPlace("cl"), new ColorToken(new Color("c0")), new HLCommitmentSet()));
//        decisions.add(new HLSysDecision(hlgame.getPlace("buff"), new ColorToken(new Color("c0")), new HLCommitmentSet(
//                new ColoredTransition(hlgame, hlgame.getTransition("t2"), val),
//                new ColoredTransition(hlgame, hlgame.getTransition("t3"), val)
//        )));

        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
//        HLDecisionSet dcs = new HLDecisionSet(decisions, true, true, game);
//        Logger.getInstance().addMessage(dcs.toString());
//        Logger.getInstance().addMessage(dcs.calcNdet(decisions));

        GameGraphByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
//        HLTools.saveGraph2PDF(outputDir + "DWs1HL_gg", graph);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graphll = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        HLTools.saveGraph2PDF(outputDir + "DWs1LL_gg", graphll);
        Logger.getInstance().addMessage("size HL" + graphll.getStatesView().size());

//        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
//        PetriGame llgame = HL2PGConverter.convert(hlgame, true, true);
//        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(llgame, syms, false, new Safety(), new BDDSolverOptions());
//        sol.initialize();
//
//        double size = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
//        Logger.getInstance().addMessage("size " + size);
//        BDDGraph graph = sol.getGraphGame();
//        BDDTools.saveGraph2PDF(outputDir + "CM21_gg", graph, sol);
    }

    @Test
    public void testPD() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        int packages = 1;
        int drones = 4;
        HLPetriGame hlgame = PackageDeliveryHL.generateEwithPool(packages, drones, true);
        HLTools.saveHLPG2PDF(outputDir + "PD16HL", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        GameGraphByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
//        HLTools.saveGraph2PDF(outputDir + "DWs1HL_gg", graph);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = PackageDeliveryHL.generateEwithPool(packages, drones, true);
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graphll = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        HLTools.saveGraph2PDF(outputDir + "DWs1LL_gg", graphll);
        Logger.getInstance().addMessage("size LL" + graphll.getStatesView().size());

        hlgame = PackageDeliveryHL.generateEwithPool(packages, drones, true);
        game = new OneEnvHLPG(hlgame, false);

        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graph2 = SGGBuilderHL.getInstance().create(game);
//        HLTools.saveGraph2PDF(outputDir + "DWs1HL_gg", graph);
        Logger.getInstance().addMessage("size HL" + graph2.getStatesView().size());

        hlgame = PackageDeliveryHL.generateEwithPool(packages, drones, true);
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graphll2 = SGGBuilderLL.getInstance().create(hlgame);
//        HLTools.saveGraph2PDF(outputDir + "DWs1LL_gg", graphll);
        Logger.getInstance().addMessage("size LL" + graphll2.getStatesView().size());

//        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
//        PetriGame llgame = HL2PGConverter.convert(hlgame, true, true);
//        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(llgame, syms, false, new Safety(), new BDDSolverOptions());
//        sol.initialize();
//
//        double size = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
//        Logger.getInstance().addMessage("size " + size);
//        BDDGraph graph = sol.getGraphGame();
//        BDDTools.saveGraph2PDF(outputDir + "CM21_gg", graph, sol);
    }

    @Test
    public void testCM() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(4, 1, true);
        HLTools.saveHLPG2PDF(outputDir + "CM41HL", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        GameGraphByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
        HLTools.saveGraph2PDF(outputDir + "CM41HL_gg", graph);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graphll = SGGBuilderLL.getInstance().createByHashcode(hlgame);
        HLTools.saveGraph2DotAndPDF(outputDir + "CM41LL_gg", graphll);
        Logger.getInstance().addMessage("size HL" + graphll.getStatesView().size());

        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        PetriGameWithTransits llgame = HL2PGConverter.convert(hlgame, true, true);
        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(new DistrSysBDDSolvingObject<>(llgame, new Safety()), syms, new BDDSolverOptions(false));
        sol.initialize();
//
        double size = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        Logger.getInstance().addMessage("size bdd " + size);
        BDDGraph bddGraph = sol.getGraphGame();
        Logger.getInstance().addMessage("size bdd by graph " + bddGraph.getStates().size());
        BDDTools.saveGraph2DotAndPDF(outputDir + "C41_bdd_gg", bddGraph, sol);
    }

    @Test
    public void hlVsll() throws ModuleException, FileNotFoundException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
        HLTools.saveHLPG2PDF(outputDir + "CM32HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        GameGraphByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
//        HLTools.saveGraph2PDF(outputDir + "CM41HL_gg", graph);
        int hlmcuts = 0;
        int hlbad = 0;
        for (HLDecisionSet state : graph.getStatesView()) {
            if (state.isMcut()) {
                ++hlmcuts;
            }
            if (state.isBad()) {
                ++hlbad;
            }
        }
        Collection<ColoredTransition> systemTransitions = new ArrayList<>();
        for (Transition t : game.getSystemTransitions()) {
            for (ValuationIterator it = hlgame.getValuations(t).iterator(); it.hasNext();) {
                Valuation val = it.next();
                ColoredTransition ct = new ColoredTransition(hlgame, t, val);
                if (ct.isValid()) {
                    systemTransitions.add(ct);
                }
            }
        }

        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graphll = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM41LL_gg", graphll);
        int llmcuts = 0;
        int llbad = 0;
        for (DecisionSet state : graphll.getStatesView()) {
            if (state.isMcut()) {
                ++llmcuts;
            }
            if (state.isBad()) {
                ++llbad;
            }
        }
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true);
        // calculate the system transitions
        Collection<Transition> sysTransitions = new ArrayList<>();
        Collection<Transition> singlePresetTransitions = new ArrayList<>();
        for (Transition transition : pgame.getTransitions()) {
            boolean isSystem = true;
            for (Place place : transition.getPreset()) {
                if (pgame.isEnvironment(place)) {
                    isSystem = false;
                }
            }
            if (isSystem) {
                sysTransitions.add(transition);
                if (transition.getPreset().size() == 1) {
                    singlePresetTransitions.add(transition);
                }
            }
        }

        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLB", hlgame);
        Logger.getInstance().addMessage("size LL " + graphll.getStatesView().size());
        Logger.getInstance().addMessage("mcuts HL " + hlmcuts);
        Logger.getInstance().addMessage("mcuts LL " + llmcuts);
        Logger.getInstance().addMessage("bad HL " + hlbad);
        Logger.getInstance().addMessage("bad LL " + llbad);
        Logger.getInstance().addMessage("singleSys HL " + game.getSinglePresetTransitions().size());
        Logger.getInstance().addMessage("singleSys LL " + singlePresetTransitions.size());
        Logger.getInstance().addMessage("system HL " + systemTransitions.size());
        Logger.getInstance().addMessage("system LL " + sysTransitions.size());
    }

    @Test
    public void checkMultipleCallsHLByHash() throws ModuleException, FileNotFoundException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        AbstractGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, IntegerID, GameGraphFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());
    }

    @Test
    public void checkMultipleCallsHL() throws ModuleException, FileNotFoundException {
        int machines = 3;
        int products = 2;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        AbstractGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());
    }

    @Test
    public void checkMultipleCallsLLByHash() throws ModuleException, FileNotFoundException {
        int machines = 2;
        int products = 1;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());
    }

    @Test
    public void checkMultipleCallsLL() throws ModuleException, FileNotFoundException {
        int machines = 2;
        int products = 1;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());
    }

    @Test
    public void checkDW() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        int clerks = 4;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graph = SGGBuilderLL.getInstance().create(hlgame);
        Logger.getInstance().addMessage("size LL " + graph.getStatesView().size());

//        hlgame = DocumentWorkflowHL.generateDW(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graphhash = SGGBuilderLL.getInstance().createByHashcode(hlgame);
        Logger.getInstance().addMessage("size LL hash" + graphhash.getStatesView().size());

        //%%%%
//        hlgame = DocumentWorkflowHL.generateDW(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graphHL = SGGBuilderHL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL " + graphHL.getStatesView().size());

//        hlgame = DocumentWorkflowHL.generateDW(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
//        game = new OneEnvHLPG(hlgame, false);
        GameGraphByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, IntegerID>> graphHLhash = SGGBuilderHL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL hash" + graphHLhash.getStatesView().size());
    }

    @Test
    public void checkDWs() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        int clerks = 3;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graph = SGGBuilderLL.getInstance().create(hlgame);
        Logger.getInstance().addMessage("size LL " + graph.getStatesView().size());

//        hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        GameGraphByHashCode<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, IntegerID>> graphhash = SGGBuilderLL.getInstance().createByHashcode(hlgame);
        Logger.getInstance().addMessage("size LL hash" + graphhash.getStatesView().size());

        //%%%%
//        hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graphHL = SGGBuilderHL.getInstance().create(game);
        Logger.getInstance().addMessage("size HL " + graphHL.getStatesView().size());

//        hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
//        game = new OneEnvHLPG(hlgame, false);
        GameGraphByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, IntegerID>> graphHLhash = SGGBuilderHL.getInstance().createByHashcode(game);
        Logger.getInstance().addMessage("size HL hash" + graphHLhash.getStatesView().size());
    }

    @Test
    public void alarmSystem() throws ModuleException, FileNotFoundException, NotSupportedGameException, NetNotSafeException, InvalidPartitionException, NoSuitableDistributionFoundException, CalculationInterruptedException {
        HLPetriGame hlgame = AlarmSystemHL.createSafetyVersionForHLRepWithSetMinus(2, true);
//        HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(3, true); // fits
//        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(3, true);// fits
//        HLPetriGame hlgame = PackageDeliveryHL.generateEwithPool(2, 2, true);// fits
        HLTools.saveHLPG2PDF(outputDir + "AS2HL", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        GameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, GameGraphFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
//        HLTools.saveGraph2PDF(outputDir + "CM41HL_gg", graph);
        int hlmcuts = 0;
        int hlbad = 0;
        for (HLDecisionSet state : graph.getStatesView()) {
            if (state.isMcut()) {
                ++hlmcuts;
            }
            if (state.isBad()) {
                ++hlbad;
            }
        }
        Collection<ColoredTransition> systemTransitions = new ArrayList<>();
        for (Transition t : game.getSystemTransitions()) {
            for (ValuationIterator it = hlgame.getValuations(t).iterator(); it.hasNext();) {
                Valuation val = it.next();
                ColoredTransition ct = new ColoredTransition(hlgame, t, val);
                if (ct.isValid()) {
                    systemTransitions.add(ct);
                }
            }
        }

        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        GameGraph<Place, Transition, ILLDecision, DecisionSet, GameGraphFlow<Transition, DecisionSet>> graphll = SGGBuilderLL.getInstance().create(hlgame);
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM41LL_gg", graphll);
        int llmcuts = 0;
        int llbad = 0;
        for (DecisionSet state : graphll.getStatesView()) {
            if (state.isMcut()) {
                ++llmcuts;
            }
            if (state.isBad()) {
                ++llbad;
            }
        }
        PetriGameWithTransits pgame = HL2PGConverter.convert(hlgame, true);
        // calculate the system transitions
        Collection<Transition> sysTransitions = new ArrayList<>();
        Collection<Transition> singlePresetTransitions = new ArrayList<>();
        for (Transition transition : pgame.getTransitions()) {
            boolean isSystem = true;
            for (Place place : transition.getPreset()) {
                if (pgame.isEnvironment(place)) {
                    isSystem = false;
                }
            }
            if (isSystem) {
                sysTransitions.add(transition);
                if (transition.getPreset().size() == 1) {
                    singlePresetTransitions.add(transition);
                }
            }
        }

        PetriGameWithTransits bddgame = HL2PGConverter.convert(hlgame, true, true);
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        BDDSolverOptions opt = new BDDSolverOptions(true);
        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(new DistrSysBDDSolvingObject<>(bddgame, new Safety()), syms, opt);
        sol.initialize();

        BDD states = sol.getBufferedDCSs();
        double sizeBDD = states.satCount(sol.getFirstBDDVariables()) + 1;
        BDD mcuts = sol.getMcut().and(states);
        BDD bad = sol.badStates().and(states);

        Logger.getInstance().addMessage("size HL" + graph.getStatesView().size());
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLB", hlgame);
        Logger.getInstance().addMessage("size LL " + graphll.getStatesView().size());
        Logger.getInstance().addMessage("size BDD " + sizeBDD);
        Logger.getInstance().addMessage("mcuts HL " + hlmcuts);
        Logger.getInstance().addMessage("mcuts LL " + llmcuts);
        Logger.getInstance().addMessage("mcuts BDD " + mcuts.satCount(sol.getFirstBDDVariables()));
        Logger.getInstance().addMessage("bad HL " + hlbad);
        Logger.getInstance().addMessage("bad LL " + llbad);
        Logger.getInstance().addMessage("badd BDD " + bad.satCount(sol.getFirstBDDVariables()));
        Logger.getInstance().addMessage("singleSys HL " + game.getSinglePresetTransitions().size());
        Logger.getInstance().addMessage("singleSys LL " + singlePresetTransitions.size());
        Logger.getInstance().addMessage("system HL " + systemTransitions.size());
        Logger.getInstance().addMessage("system LL " + sysTransitions.size());

    }
}
