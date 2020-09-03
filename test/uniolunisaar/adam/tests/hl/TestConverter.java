package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.parser.ParseException;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.objectives.Safety;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.NotSupportedGameException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.exceptions.pnwt.NetNotSafeException;
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.generators.hl.ContainerHabourHL;
import uniolunisaar.adam.generators.hl.PackageDeliveryHL;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
import uniolunisaar.adam.logic.pg.solver.hl.bddapproach.BDDASafetyWithoutType2HLSolver;
import uniolunisaar.adam.ds.graph.symbolic.bddapproach.BDDGraph;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.BDDSolverOptions;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolverFactory;
import uniolunisaar.adam.ds.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolvingObject;
import uniolunisaar.adam.logic.pg.solver.symbolic.bddapproach.distrsys.DistrSysBDDSolver;
import uniolunisaar.adam.util.symbolic.bddapproach.BDDTools;
import uniolunisaar.adam.tools.Logger;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestConverter {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/converter/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    @Test
    public void toyExample() throws FileNotFoundException, IOException, InterruptedException {
        HLPetriGame hlgame = new HLPetriGame("test");
        hlgame.createBasicColorClass("C", false, "a0", "a1");
        Place p1 = hlgame.createSysPlace(new String[]{"C"});
        Place p2 = hlgame.createSysPlace(new String[]{"C", "C"});
        Transition t = hlgame.createTransition();
        hlgame.createFlow(t, p2, new ArcExpression(new ArcTuple(new Variable("a"), new Variable("b"))));
        ArcExpression expr = new ArcExpression();
        expr.add(new Variable("a"));
        expr.add(new Variable("b"));
        hlgame.createFlow(p1, t, expr);
        HLTools.saveHLPG2PDF(outputDir + hlgame.getName(), hlgame);
        PetriGame pg = HL2PGConverter.convert(hlgame);
        PGTools.savePG2PDF(outputDir + pg.getName(), pg, false);
    }

    @Test
    public void alarmSystem() throws IOException, InterruptedException {
        HLPetriGame hlgame = AlarmSystemHL.createSafetyVersionForHLRep(3, true);
        HLTools.saveHLPG2PDF(outputDir + hlgame.getName(), hlgame);
        PetriGame pg = HL2PGConverter.convert(hlgame);
        PGTools.savePG2PDF(outputDir + pg.getName(), pg, false);
    }

    @Test
    public void containerHabour() throws IOException, InterruptedException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, RenderException {
//        HLPetriGame hlgame = ContainerHabourHL.generateB(2, 2, 2, 1, true);
//        HLTools.saveHLPG2PDF(outputDir + hlgame.getName(), hlgame);
//        PetriGame pg = HL2PGConverter.convert(hlgame, true, true);
//        PGTools.savePG2PDF(outputDir + pg.getName(), pg, false);
//        PGTools.saveAPT(outputDir + pg.getName(), pg, false);
//        BDDSolverOptions opt = new BDDSolverOptions();
//        opt.setNoType2(true);
//        BDDSolver<? extends Condition> sol = BDDSolverFactory.getInstance().getSolver(pg, false, opt);
//        System.out.println("asdf " + sol.existsWinningStrategy());
//        
//        BDDTools.saveGraph2PDF(outputDir + "habour_gg", sol.getGraphGame(), sol);

//        HLPetriGame hlgame = ContainerHabourHL.generateC(2, 2, 1, 1, 1, true);
//        HLTools.saveHLPG2PDF(outputDir + hlgame.getName(), hlgame);
//        PetriGame pg = HL2PGConverter.convert(hlgame, true, true);
//        PGTools.savePG2PDF(outputDir + pg.getName(), pg, false);
//        PGTools.saveAPT(outputDir + pg.getName(), pg, false);
//        BDDSolverOptions opt = new BDDSolverOptions();
//        opt.setNoType2(true);
//        BDDSolver<? extends Condition> sol = BDDSolverFactory.getInstance().getSolver(pg, false, opt);
//        System.out.println("asdf " + sol.existsWinningStrategy());
        HLPetriGame hlgame = ContainerHabourHL.generateD(4, 2, 1, 1, true);
        HLTools.saveHLPG2PDF(outputDir + hlgame.getName(), hlgame);
        PetriGame pg = HL2PGConverter.convert(hlgame, true, true);
        PGTools.savePG2PDF(outputDir + pg.getName(), pg, false, true);
        PGTools.saveAPT(outputDir + pg.getName(), pg, true);
        BDDSolverOptions opt = new BDDSolverOptions(false);
        opt.setNoType2(true);
        DistrSysBDDSolver<? extends Condition<?>> sol = DistrSysBDDSolverFactory.getInstance().getSolver(pg, opt);

        sol.initialize();

        double sizeBDD = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        System.out.println("size" + sizeBDD);
//        System.out.println("asdf " + sol.existsWinningStrategy());

    }

    @Test
    public void packageDelivery() throws IOException, InterruptedException, CouldNotFindSuitableConditionException, SolvingException, CalculationInterruptedException, RenderException, NotSupportedGameException, ParseException, NetNotSafeException {
        Logger.getInstance().setVerbose(true);

        HLPetriGame hlgame = PackageDeliveryHL.generateE(1, 3, true);
        HLTools.saveHLPG2PDF(outputDir + hlgame.getName(), hlgame);
        PetriGame pg = HL2PGConverter.convert(hlgame, true, true);
        PGTools.savePG2PDF(outputDir + pg.getName(), pg, false, true);
        PGTools.saveAPT(outputDir + pg.getName(), pg, true);

        BDDSolverOptions opt = new BDDSolverOptions(true);
        opt.setNoType2(true);
        DistrSysBDDSolver<? extends Condition<?>> sol = DistrSysBDDSolverFactory.getInstance().getSolver(PGTools.getPetriGameFromParsedPetriNet(pg, true, false), opt);
        sol.initialize();

        double sizeBDDLow = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1; // for the additional init state

//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% sizeLL: " + sizeBDDLow);
//        System.out.println("asdf " + sol.existsWinningStrategy());
        BDDGraph bddgraph = sol.getGraphGame();
        BDDTools.saveGraph2PDF(outputDir + "PDLL13_gg", bddgraph, sol);

//        BDDSolverOptions opt = new BDDSolverOptions();
//        opt.setNoType2(true);
//        DistrSysBDDSolver<? extends Condition<?>> sol = BDDSolverFactory.getInstance().getSolver(pg, false, opt);
        Symmetries syms = new Symmetries(hlgame.getBasicColorClasses());
        opt = new BDDSolverOptions(false);
        BDDASafetyWithoutType2HLSolver solBDD = new BDDASafetyWithoutType2HLSolver(new DistrSysBDDSolvingObject<>(pg, new Safety()), syms, opt);
        solBDD.initialize();

        double sizeBDD = solBDD.getBufferedDCSs().satCount(solBDD.getFirstBDDVariables()) + 1;
//        System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% size" + sizeBDD);
//        System.out.println("asdf " + solBDD.existsWinningStrategy());        
        BDDGraph bddgraphHL = solBDD.getGraphGame();
        BDDTools.saveGraph2PDF(outputDir + "PDHL13_gg", bddgraphHL, solBDD);
    }

}
