package uniolunisaar.adam.generators.hl;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.predicate.BasicPredicate;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class DocumentWorkflowHL {

    /**
     * Creates the HL version of DW benchmark (originally CAV2015) for the RvG
     * Festschrift.
     *
     * @param size
     * @return
     */
    public static HLPetriGame generateDW(int size) {
        if (size < 1) {
            throw new RuntimeException("There should at least be one Clerk to sign the document.");
        }
        HLPetriGame net = new HLPetriGame("High-Level Document Workflow with " + size + " clerks. (DW)");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // create the color classes
        Color[] c = new Color[size];
        for (int i = 0; i < c.length; i++) {
            c[i] = new Color("c" + i);
        }
        net.createBasicColorClass("E", false, "e");
        net.createBasicColorClass("N", true, c);
        String[] ec = {"E"};
        String[] nc = {"N"};

        Place start = net.createEnvPlace("start", ec);
        net.setColorTokens(start, "e");
        Place firstClerk = net.createEnvPlace("clerk", nc); // todo: do I really want to have an env place with different token? Does not need to be an env place

        Transition t = net.createTransition();
        net.createFlow(start, t, new ArcExpression(new Variable("e")));
        net.createFlow(t, firstClerk);

        Place cl = net.createSysPlace("cl", nc);
        net.setColorTokens(cl, c);
        Place vote = net.createSysPlace("vote", nc);
        Place first = net.createEnvPlace("first", nc);
        Place yes = net.createSysPlace("Y", nc);
        Place no = net.createSysPlace("N", nc);
        Place end = net.createSysPlace("end", nc);

        Transition t1 = net.createTransition();
        Transition t2 = net.createTransition();
        Transition t3 = net.createTransition();
        Transition glue = net.createTransition("glue");

        //t1
        net.createFlow(t1, vote);
        net.createFlow(t1, first);
        net.createFlow(firstClerk, t1);
        net.createFlow(cl, t1);
        //t2
        net.createFlow(vote, t2);
        net.createFlow(t2, yes);
        net.createFlow(t2, end);
        //t3
        net.createFlow(vote, t3);
        net.createFlow(t3, no);
        net.createFlow(t3, end);
        // Glue
        ArcExpression succ = new ArcExpression(new SuccessorTerm(new Variable("x"), net));
        net.createFlow(end, glue);
        net.createFlow(glue, vote, succ);
        net.createFlow(cl, glue, succ);

        // Bad place     
        // Did all clerks have signed the note?
        Place ready = net.createSysPlace("ready", ec);
        // are they all Y or all N?
        Place good = net.createSysPlace("good", ec);

        // they are not all Y or all N
        Place bad = net.createSysPlace("bad", ec);
        net.setBad(bad);
        ArcExpression e = new ArcExpression(new Variable("e"));
        Transition tbad = net.createTransition("tbad");
        net.createFlow(ready, tbad, e);
        net.createFlow(tbad, bad, e);

        Transition tGoodReady = net.createTransition();
        net.createFlow(ready, tGoodReady);
        net.createFlow(good, tGoodReady);

        Transition yesT = net.createTransition("yes");
        net.createFlow(yesT, good, e);
        net.createFlow(yes, yesT, new ArcExpression(new ColorClassTerm("N")));
        Transition noT = net.createTransition("no");
        net.createFlow(noT, good, e);
        net.createFlow(no, noT, new ArcExpression(new ColorClassTerm("N")));
        //  Did all clerks have signed the note?
        Transition tReady = net.createTransition("t_ready", new BasicPredicate<>(new Variable("x"), BasicPredicate.Operator.EQ, new SuccessorTerm(new Variable("y"), net)));
        net.createFlow(tReady, ready, new ArcExpression(new Variable("e")));
        net.createFlow(first, tReady, new ArcExpression(new Variable("x"))); // would be the same to use succ(y) without the predicate 
        net.createFlow(end, tReady, new ArcExpression(new Variable("y")));
        return net;
    }

    /**
     * Creates the HL version of DWs benchmark (originally CAV2015) for the RvG
     * Festschrift.
     *
     * @param size
     * @return
     */
    public static HLPetriGame generateDWs(int size) {
        if (size < 1) {
            throw new RuntimeException("There should be at least one Clerk to sign the document.");
        }
        HLPetriGame game = new HLPetriGame("High-Level Document Workflow with " + size + " clerks. (DW)");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // create the color classes
        Color[] c = new Color[size];
        for (int i = 0; i < c.length; i++) {
            c[i] = new Color("c" + i);
        }
        game.createBasicColorClass("E", false, "e");
        game.createBasicColorClass("N", true, c);
        String[] ec = {"E"};
        String[] nc = {"N"};

        Place e1 = game.createEnvPlace("e1", nc);
        Place e2 = game.createEnvPlace("e2", nc);
        Place start = game.createEnvPlace("start", ec);
        game.setColorTokens(start, "e");

        ArcExpression e = new ArcExpression(new Variable("e"));
        Transition t = game.createTransition();
        game.createFlow(start, t, e);
        game.createFlow(t, e1);

        Place cl = game.createSysPlace("cl", nc);
        game.setColorTokens(cl, c);
        Place vote = game.createSysPlace("vote", nc);
        Place yes = game.createSysPlace("Y", nc);
        Place no = game.createSysPlace("N", nc);
        Place end = game.createSysPlace("end", nc);
        Place bad = game.createSysPlace("bad", nc);
        game.setBad(bad);
        Place buf0 = game.createSysPlace("buf", nc);
        Place buf1 = game.createSysPlace("buff", nc);
        game.setColorTokens(buf1, c);

        Transition t1 = game.createTransition();
        Transition t2 = game.createTransition();
        Transition t3 = game.createTransition();
        Transition glue = game.createTransition("glue");
        Transition badT = game.createTransition("tbad");

        //t1
        game.createFlow(t1, vote);
        game.createFlow(t1, e2);
        game.createFlow(e1, t1);
        game.createFlow(cl, t1);
        //t2
        game.createFlow(buf1, t2);
        game.createFlow(vote, t2);
        game.createFlow(t2, yes);
        game.createFlow(t2, end);
        //t3
        game.createFlow(buf1, t3);
        game.createFlow(vote, t3);
        game.createFlow(t3, no);
        game.createFlow(t3, end);
        //t5        
        ArcExpression succ = new ArcExpression(new SuccessorTerm(new Variable("x"), game));
        game.createFlow(glue, vote, succ);
        game.createFlow(glue, buf0, succ);
        game.createFlow(cl, glue, succ);
        game.createFlow(end, glue);
        //tbad
        game.createFlow(no, badT);
        game.createFlow(badT, bad);
        return game;
    }

}
