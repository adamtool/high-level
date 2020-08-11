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
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;
import uniolunisaar.adam.ds.highlevel.ColorTokens;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.predicate.BasicPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.BinaryPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.UnaryPredicate;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.petrinet.PetriNetExtensionHandler;
import uniolunisaar.adam.pnml.PnmlRenderer;
import uniolunisaar.adam.util.HLTools;
import uniolunisaar.adam.util.PGTools;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

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
    public void symmtricNet() throws FileNotFoundException, IOException, InterruptedException, TransformerException, ParserConfigurationException {
        HLPetriGame hlgame = new HLPetriGame("Access Policy");

        // color classes
        List<Pair<String, String[]>> staticSubClasses = new ArrayList<>();
        staticSubClasses.add(new Pair<>("wwwId", new String[]{"apacheId", "iisId"}));
        staticSubClasses.add(new Pair<>("workId", new String[]{"chrisId", "deniseId"}));
        staticSubClasses.add(new Pair<>("adminId", new String[]{"rootId"}));
        hlgame.createBasicColorClassByStaticSubClass("usersnamed", false, staticSubClasses);

        hlgame.createBasicColorClass("filesnamed", false, "indexId", "emacsId", "articleId", "etc-passwdId");

        String[] filesusersnamed = new String[]{"filesnamed", "usersnamed"};
        String[] usersfilesnamed = new String[]{"usersnamed", "filesnamed"};

        // variables should not needed to be declared here
        // places
        Place p1 = hlgame.createSysPlace("p1", new String[]{"usersnamed"});
        List<Color> usersnamedColors = hlgame.getBasicColorClass("usersnamed").getColors();
        Color[] unc = new Color[usersnamedColors.size()];
        hlgame.setColorTokens(p1, usersnamedColors.toArray(unc));
        PetriNetExtensionHandler.setXCoord(p1, 23);
        PetriNetExtensionHandler.setYCoord(p1, 23);
        PetriNetExtensionHandler.setLabel(p1, "Users");

        Place p2 = hlgame.createSysPlace("p2", filesusersnamed);
        PetriNetExtensionHandler.setXCoord(p2, 45);
        PetriNetExtensionHandler.setYCoord(p2, 23);
        PetriNetExtensionHandler.setLabel(p2, "Files");
        ColorTokens tokens = new ColorTokens();
        tokens.add(new ColorToken(new Color("indexId"), new Color("apacheId")));
        tokens.add(new ColorToken(new Color("emacsId"), new Color("chrisId")));
        tokens.add(new ColorToken(new Color("articleId"), new Color("chrisId")));
        tokens.add(new ColorToken(new Color("etc-passwdId"), new Color("rootId")));
        hlgame.setColorTokens(p2, tokens);

        Place p3 = hlgame.createSysPlace("p3", usersfilesnamed);
        PetriNetExtensionHandler.setXCoord(p3, 34);
        PetriNetExtensionHandler.setYCoord(p3, 50);
        PetriNetExtensionHandler.setLabel(p3, "FilesBeingAccessed");

        Transition t1 = hlgame.createTransition("t1");
        PetriNetExtensionHandler.setXCoord(t1, 34);
        PetriNetExtensionHandler.setYCoord(t1, 33);
        t1.setLabel("Grant Access");
        IPredicate pred = new BinaryPredicate(
                new BasicPredicate<>(new Variable("u"), BasicPredicate.Operator.EQ, new Variable("v")),
                BinaryPredicate.Operator.OR,
                // we dont have the gtp (greater than partition (static subclass operator, so here just some crap for testing s.th.)
                new UnaryPredicate(UnaryPredicate.Operator.NEG, new BinaryPredicate(new BasicPredicate<>(new Variable("u"), BasicPredicate.Operator.EQ, new Variable("v")), BinaryPredicate.Operator.AND, new BasicPredicate<>(new Variable("v"), BasicPredicate.Operator.EQ, new Variable("u"))))
        );
        hlgame.setPredicate(t1, pred);

        hlgame.createFlow(p1, t1, new ArcExpression(new Variable("u")));
        hlgame.createFlow(p2, t1, new ArcExpression(new ArcTuple(new Variable("f"), new Variable("v"))));
        hlgame.createFlow(t1, p3, new ArcExpression(new ArcTuple(new Variable("u"), new Variable("f"))));

        HLTools.saveHLPG2PDF(outputDir + hlgame.getName(), hlgame, true);
        PetriGame pg = HL2PGConverter.convert(hlgame);
        PGTools.savePG2PDF(outputDir + pg.getName(), pg, false);
        new PnmlRenderer().render(hlgame, System.out);
    }

}
