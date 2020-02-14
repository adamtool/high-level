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
import uniolunisaar.adam.ds.graph.hl.AbstractSymbolicGameGraph;
import uniolunisaar.adam.ds.graph.hl.IntegerID;
import uniolunisaar.adam.ds.graph.hl.SGG;
import uniolunisaar.adam.ds.graph.hl.SGGFlow;
import uniolunisaar.adam.ds.graph.hl.SGGByHashCode;
import uniolunisaar.adam.ds.graph.hl.approachHL.HLDecisionSet;
import uniolunisaar.adam.ds.graph.hl.approachHL.IHLDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.ILLDecision;
import uniolunisaar.adam.ds.graph.hl.approachLL.LLDecisionSet;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.ValuationIterator;
import uniolunisaar.adam.ds.highlevel.oneenv.OneEnvHLPG;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.petrinet.objectives.Safety;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.hl.DocumentWorkflowHL;
import uniolunisaar.adam.generators.hl.PackageDeliveryHL;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.graphbuilder.hl.SGGBuilderHL;
import uniolunisaar.adam.logic.graphbuilder.hl.SGGBuilderLL;
import uniolunisaar.adam.logic.solver.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.BDDSolverOptions;
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
//        Logger.getInstance().setVerbose(false);
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
//        System.out.println(dcs.toString());
//        System.out.println(dcs.calcNdet(decisions));

        SGGByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
//        HLTools.saveGraph2PDF(outputDir + "DWs1HL_gg", graph);
        System.out.println("size HL" + graph.getStatesView().size());

        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graphll = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        HLTools.saveGraph2PDF(outputDir + "DWs1LL_gg", graphll);
        System.out.println("size HL" + graphll.getStatesView().size());

//        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
//        PetriGame llgame = HL2PGConverter.convert(hlgame, true, true);
//        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(llgame, syms, false, new Safety(), new BDDSolverOptions());
//        sol.initialize();
//
//        double size = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
//        System.out.println("size " + size);
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

        SGGByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
//        HLTools.saveGraph2PDF(outputDir + "DWs1HL_gg", graph);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = PackageDeliveryHL.generateEwithPool(packages, drones, true);
        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graphll = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        HLTools.saveGraph2PDF(outputDir + "DWs1LL_gg", graphll);
        System.out.println("size LL" + graphll.getStatesView().size());

        hlgame = PackageDeliveryHL.generateEwithPool(packages, drones, true);
        game = new OneEnvHLPG(hlgame, false);

        SGG<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> graph2 = SGGBuilderHL.getInstance().create(game);
//        HLTools.saveGraph2PDF(outputDir + "DWs1HL_gg", graph);
        System.out.println("size HL" + graph2.getStatesView().size());

        hlgame = PackageDeliveryHL.generateEwithPool(packages, drones, true);
        SGG<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> graphll2 = SGGBuilderLL.getInstance().create(hlgame);
//        HLTools.saveGraph2PDF(outputDir + "DWs1LL_gg", graphll);
        System.out.println("size LL" + graphll2.getStatesView().size());

//        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
//        PetriGame llgame = HL2PGConverter.convert(hlgame, true, true);
//        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(llgame, syms, false, new Safety(), new BDDSolverOptions());
//        sol.initialize();
//
//        double size = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
//        System.out.println("size " + size);
//        BDDGraph graph = sol.getGraphGame();
//        BDDTools.saveGraph2PDF(outputDir + "CM21_gg", graph, sol);
    }

    @Test
    public void testCM() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(4, 1, true);
        HLTools.saveHLPG2PDF(outputDir + "CM41HL", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        SGGByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
        HLTools.saveGraph2PDF(outputDir + "CM41HL_gg", graph);
        System.out.println("size HL" + graph.getStatesView().size());

        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graphll = SGGBuilderLL.getInstance().createByHashcode(hlgame);
        HLTools.saveGraph2DotAndPDF(outputDir + "CM41LL_gg", graphll);
        System.out.println("size HL" + graphll.getStatesView().size());

        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        PetriGame llgame = HL2PGConverter.convert(hlgame, true, true);
        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(llgame, syms, false, new Safety(), new BDDSolverOptions());
        sol.initialize();
//
        double size = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        System.out.println("size bdd " + size);
        BDDGraph bddGraph = sol.getGraphGame();
        System.out.println("size bdd by graph " + bddGraph.getStates().size());
        BDDTools.saveGraph2DotAndPDF(outputDir + "C41_bdd_gg", bddGraph, sol);
    }

    @Test
    public void hlVsll() throws ModuleException, FileNotFoundException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        SGGByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
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
        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graphll = SGGBuilderLL.getInstance().createByHashcode(hlgame);
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM41LL_gg", graphll);
        int llmcuts = 0;
        int llbad = 0;
        for (LLDecisionSet state : graphll.getStatesView()) {
            if (state.isMcut()) {
                ++llmcuts;
            }
            if (state.isBad()) {
                ++llbad;
            }
        }
        PetriGame pgame = HL2PGConverter.convert(hlgame, true);
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

        System.out.println("size HL" + graph.getStatesView().size());
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLB", hlgame);
        System.out.println("size LL " + graphll.getStatesView().size());
        System.out.println("mcuts HL " + hlmcuts);
        System.out.println("mcuts LL " + llmcuts);
        System.out.println("bad HL " + hlbad);
        System.out.println("bad LL " + llbad);
        System.out.println("singleSys HL " + game.getSinglePresetTransitions().size());
        System.out.println("singleSys LL " + singlePresetTransitions.size());
        System.out.println("system HL " + systemTransitions.size());
        System.out.println("system LL " + sysTransitions.size());
    }

    @Test
    public void checkMultipleCallsHLByHash() throws ModuleException, FileNotFoundException {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        AbstractSymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, IntegerID, SGGFlow<ColoredTransition, IntegerID>> graph = SGGBuilderHL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3, 2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());
    }

    @Test
    public void checkMultipleCallsHL() throws ModuleException, FileNotFoundException {
        int machines = 3;
        int products = 2;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        AbstractSymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderHL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());
    }

    @Test
    public void checkMultipleCallsLLByHash() throws ModuleException, FileNotFoundException {
        int machines = 2;
        int products = 1;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().createByHashcode(game);
        System.out.println("size HL" + graph.getStatesView().size());
    }

    @Test
    public void checkMultipleCallsLL() throws ModuleException, FileNotFoundException {
        int machines = 2;
        int products = 1;
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        SGG<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());

        hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(machines, products, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        game = new OneEnvHLPG(hlgame, false);
        graph = SGGBuilderLL.getInstance().create(game);
        System.out.println("size HL" + graph.getStatesView().size());
    }

    @Test
    public void checkDW() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        int clerks = 4;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        SGG<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> graph = SGGBuilderLL.getInstance().create(hlgame);
        System.out.println("size LL " + graph.getStatesView().size());

//        hlgame = DocumentWorkflowHL.generateDW(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graphhash = SGGBuilderLL.getInstance().createByHashcode(hlgame);
        System.out.println("size LL hash" + graphhash.getStatesView().size());

        //%%%%
//        hlgame = DocumentWorkflowHL.generateDW(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        SGG<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> graphHL = SGGBuilderHL.getInstance().create(game);
        System.out.println("size HL " + graphHL.getStatesView().size());

//        hlgame = DocumentWorkflowHL.generateDW(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
//        game = new OneEnvHLPG(hlgame, false);
        SGGByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, IntegerID>> graphHLhash = SGGBuilderHL.getInstance().createByHashcode(game);
        System.out.println("size HL hash" + graphHLhash.getStatesView().size());
    }

    @Test
    public void checkDWs() throws IOException, InterruptedException, NotSupportedGameException, NetNotSafeException, NoSuitableDistributionFoundException, InvalidPartitionException, CalculationInterruptedException {
        int clerks = 3;
        HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        SGG<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> graph = SGGBuilderLL.getInstance().create(hlgame);
        System.out.println("size LL " + graph.getStatesView().size());

//        hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        SGGByHashCode<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, IntegerID>> graphhash = SGGBuilderLL.getInstance().createByHashcode(hlgame);
        System.out.println("size LL hash" + graphhash.getStatesView().size());

        //%%%%
//        hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);
        SGG<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> graphHL = SGGBuilderHL.getInstance().create(game);
        System.out.println("size HL " + graphHL.getStatesView().size());

//        hlgame = DocumentWorkflowHL.generateDWs(clerks, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLA", hlgame);
//        game = new OneEnvHLPG(hlgame, false);
        SGGByHashCode<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, IntegerID>> graphHLhash = SGGBuilderHL.getInstance().createByHashcode(game);
        System.out.println("size HL hash" + graphHLhash.getStatesView().size());
    }

    @Test
    public void alarmSystem() throws ModuleException, FileNotFoundException, NotSupportedGameException, NetNotSafeException, InvalidPartitionException, NoSuitableDistributionFoundException, CalculationInterruptedException {
        HLPetriGame hlgame = AlarmSystemHL.createSafetyVersionForHLRepWithSetMinus(2, true);
//        HLPetriGame hlgame = DocumentWorkflowHL.generateDWs(3, true); // fits
//        HLPetriGame hlgame = DocumentWorkflowHL.generateDW(3, true);// fits
//        HLPetriGame hlgame = PackageDeliveryHL.generateEwithPool(2, 2, true);// fits
        HLTools.saveHLPG2PDF(outputDir + "AS2HL", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame, false);

        SGG<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SGGFlow<ColoredTransition, HLDecisionSet>> graph = SGGBuilderHL.getInstance().create(game);
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
        SGG<Place, Transition, ILLDecision, LLDecisionSet, SGGFlow<Transition, LLDecisionSet>> graphll = SGGBuilderLL.getInstance().create(hlgame);
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM41LL_gg", graphll);
        int llmcuts = 0;
        int llbad = 0;
        for (LLDecisionSet state : graphll.getStatesView()) {
            if (state.isMcut()) {
                ++llmcuts;
            }
            if (state.isBad()) {
                ++llbad;
            }
        }
        PetriGame pgame = HL2PGConverter.convert(hlgame, true);
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

        PetriGame bddgame = HL2PGConverter.convert(hlgame, true, true);
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());

        BDDSolverOptions opt = new BDDSolverOptions();
        BDDASafetyWithoutType2HLSolver sol = new BDDASafetyWithoutType2HLSolver(bddgame, syms, true, new Safety(), opt);
        sol.initialize();

        BDD states = sol.getBufferedDCSs();
        double sizeBDD = states.satCount(sol.getFirstBDDVariables()) + 1;
        BDD mcuts = sol.getMcut().and(states);
        BDD bad = sol.badStates().and(states);

        System.out.println("size HL" + graph.getStatesView().size());
//        HLTools.saveHLPG2PDF(outputDir + "CM41HLB", hlgame);
        System.out.println("size LL " + graphll.getStatesView().size());
        System.out.println("size BDD " + sizeBDD);
        System.out.println("mcuts HL " + hlmcuts);
        System.out.println("mcuts LL " + llmcuts);
        System.out.println("mcuts BDD " + mcuts.satCount(sol.getFirstBDDVariables()));
        System.out.println("bad HL " + hlbad);
        System.out.println("bad LL " + llbad);
        System.out.println("badd BDD " + bad.satCount(sol.getFirstBDDVariables()));
        System.out.println("singleSys HL " + game.getSinglePresetTransitions().size());
        System.out.println("singleSys LL " + singlePresetTransitions.size());
        System.out.println("system HL " + systemTransitions.size());
        System.out.println("system LL " + sysTransitions.size());

    }
}
