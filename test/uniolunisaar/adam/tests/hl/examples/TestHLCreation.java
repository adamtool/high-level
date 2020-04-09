package uniolunisaar.adam.tests.hl.examples;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
import uniolunisaar.adam.ds.highlevel.ColorTokens;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.petrinet.PetriNetExtensionHandler;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
@Test
public class TestHLCreation {

    private static final String outputDir = System.getProperty("testoutputfolder") + "/hlcreation/";

    @BeforeClass
    public void createFolder() {
//        Logger.getInstance().setVerbose(false);
//        Logger.getInstance().setShortMessageStream(null);
//        Logger.getInstance().setVerboseMessageStream(null);
//        Logger.getInstance().setWarningStream(null);
        (new File(outputDir)).mkdirs();
    }

    /**
     * Creates this net (Symmetric net simple file access model.)
     *
     * http://www.pnml.org/version-2009/examples/standardExample.pnml
     *
     * @throws FileNotFoundException
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void symmtricNet() throws FileNotFoundException, IOException, InterruptedException {
        HLPetriGame hlgame = new HLPetriGame("symmetricNet");

        // color classes
        List<Pair<String, String[]>> staticSubClasses = new ArrayList<>();
        staticSubClasses.add(new Pair<>("wwwId", new String[]{"apacheId", "iisId"}));
        staticSubClasses.add(new Pair<>("workId", new String[]{"chrisId", "deniseId"}));
        staticSubClasses.add(new Pair<>("adminId", new String[]{"rootId"}));
        hlgame.createBasicColorClassByStaticSubClass("usersnamed", false, staticSubClasses);

        hlgame.createBasicColorClass("filesnamed", false, "indexId", "emacsId", "articleId", "etc-passwdId");

        String[] filesusersnamed = new String[]{"filesnamed", "usersnamed"};
        String[] usersfilesnamednamed = new String[]{"usersnamed", "filesnamed"};

        // variables should not needed to be declared here
        // places
        Place p1 = hlgame.createSysPlace("p1", new String[]{"usersnamed"});
        hlgame.setColorTokens(p1, "apacheId", "iisId", "chrisId", "deniseId", "rootId");
        PetriNetExtensionHandler.setXCoord(p1, 23);
        PetriNetExtensionHandler.setYCoord(p1, 23);
        //could use this, but then we should think of integrating it better
        p1.putExtension("label", "Users");

        Place p2 = hlgame.createSysPlace(filesusersnamed);
        PetriNetExtensionHandler.setXCoord(p2, 45);
        PetriNetExtensionHandler.setYCoord(p2, 23);
        //could use this, but then we should think of integrating it better
        p1.putExtension("label", "Files");

        // not yet adapted to the example    
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

}
