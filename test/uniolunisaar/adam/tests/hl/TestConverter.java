package uniolunisaar.adam.tests.hl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.io.renderer.RenderException;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.exceptions.pg.CalculationInterruptedException;
import uniolunisaar.adam.exceptions.pg.SolvingException;
import uniolunisaar.adam.exceptions.pnwt.CouldNotFindSuitableConditionException;
import uniolunisaar.adam.generators.hl.AlarmSystemHL;
import uniolunisaar.adam.generators.hl.ContainerHabourHL;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolver;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverFactory;
import uniolunisaar.adam.symbolic.bddapproach.solver.BDDSolverOptions;
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
        PGTools.savePG2PDF(outputDir + pg.getName(), pg, false, 8);
        PGTools.saveAPT(outputDir + pg.getName(), pg, true);
        BDDSolverOptions opt = new BDDSolverOptions();
        opt.setNoType2(true);
        BDDSolver<? extends Condition> sol = BDDSolverFactory.getInstance().getSolver(pg, false, opt);

        sol.initialize();

        double sizeBDD = sol.getBufferedDCSs().satCount(sol.getFirstBDDVariables()) + 1;
        System.out.println("size" + sizeBDD);
//        System.out.println("asdf " + sol.existsWinningStrategy());

    }

}
