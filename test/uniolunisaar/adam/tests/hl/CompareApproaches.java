package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.SRGFlow;
import uniolunisaar.adam.ds.graph.hl.SymbolicGameGraph;
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
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.InvalidPartitionException;
import uniolunisaar.adam.exceptions.pg.NetNotSafeException;
import uniolunisaar.adam.exceptions.pg.NoSuitableDistributionFoundException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.generators.hl.ConcurrentMachinesHL;
import uniolunisaar.adam.generators.hl.DocumentWorkflowHL;
import uniolunisaar.adam.generators.hl.PackageDeliveryHL;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.hl.SGGBuilder;
import uniolunisaar.adam.logic.solver.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.symbolic.bddapproach.graph.BDDGraph;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;
import uniolunisaar.adam.symbolic.bddapproach.util.BDDTools;
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

        OneEnvHLPG game = new OneEnvHLPG(hlgame);
//        HLDecisionSet dcs = new HLDecisionSet(decisions, true, true, game);
//        System.out.println(dcs.toString());
//        System.out.println(dcs.calcNdet(decisions));

        SymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> graph = SGGBuilder.createByHLGame(game);
//        HLTools.saveGraph2PDF(outputDir + "DWs1HL_gg", graph);
        System.out.println("size HL" + graph.getStates().size());

        SymbolicGameGraph<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> graphll = SGGBuilder.createByLLGame(hlgame);
//        HLTools.saveGraph2PDF(outputDir + "DWs1LL_gg", graphll);
        System.out.println("size HL" + graphll.getStates().size());

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
        HLPetriGame hlgame = PackageDeliveryHL.generateEwithPool(1, 6, true);
        HLTools.saveHLPG2PDF(outputDir + "PD16HL", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame);

        SymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> graph = SGGBuilder.createByHLGame(game);
//        HLTools.saveGraph2PDF(outputDir + "DWs1HL_gg", graph);
        System.out.println("size HL" + graph.getStates().size());

        SymbolicGameGraph<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> graphll = SGGBuilder.createByLLGame(hlgame);
//        HLTools.saveGraph2PDF(outputDir + "DWs1LL_gg", graphll);
        System.out.println("size LL" + graphll.getStates().size());

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
        OneEnvHLPG game = new OneEnvHLPG(hlgame);

        SymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> graph = SGGBuilder.createByHLGame(game);
        HLTools.saveGraph2PDF(outputDir + "CM41HL_gg", graph);
        System.out.println("size HL" + graph.getStates().size());

        SymbolicGameGraph<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> graphll = SGGBuilder.createByLLGame(hlgame);
        HLTools.saveGraph2DotAndPDF(outputDir + "CM41LL_gg", graphll);
        System.out.println("size HL" + graphll.getStates().size());

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
    public void hlVsll() {
        HLPetriGame hlgame = ConcurrentMachinesHL.generateImprovedVersionWithSetMinus(3,2, true);
//        HLTools.saveHLPG2PDF(outputDir + "CM41HL", hlgame);
        OneEnvHLPG game = new OneEnvHLPG(hlgame);

        SymbolicGameGraph<ColoredPlace, ColoredTransition, IHLDecision, HLDecisionSet, SRGFlow<ColoredTransition>> graph = SGGBuilder.createByHLGame(game);
//        HLTools.saveGraph2PDF(outputDir + "CM41HL_gg", graph);
        int hlmcuts = 0;
        int hlbad = 0;
        for (HLDecisionSet state : graph.getStates()) {
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
                if(ct.isValid()) {
                    systemTransitions.add(ct);
                }
            }
        }

        //%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% LOW LEVEL
        SymbolicGameGraph<Place, Transition, ILLDecision, LLDecisionSet, SRGFlow<Transition>> graphll = SGGBuilder.createByLLGame(hlgame);
//        HLTools.saveGraph2DotAndPDF(outputDir + "CM41LL_gg", graphll);
        int llmcuts = 0;
        int llbad = 0;
        for (LLDecisionSet state : graphll.getStates()) {
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

        System.out.println("size HL" + graph.getStates().size());
        System.out.println("size LL " + graphll.getStates().size());
        System.out.println("mcuts HL " + hlmcuts);
        System.out.println("mcuts LL " + llmcuts);
        System.out.println("bad HL " + hlbad);
        System.out.println("bad LL " + llbad);
        System.out.println("singleSys HL " + game.getSinglePresetTransitions().size());
        System.out.println("singleSys LL " + singlePresetTransitions.size());
        System.out.println("system HL " + systemTransitions.size());
        System.out.println("system LL " + sysTransitions.size());
    }

}