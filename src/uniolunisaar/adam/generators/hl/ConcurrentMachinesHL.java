package uniolunisaar.adam.generators.hl;

import java.util.ArrayList;
import java.util.List;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.arcexpressions.SetMinusTerm;
import uniolunisaar.adam.ds.highlevel.predicate.BasicPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.Constants;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.Predicate;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class ConcurrentMachinesHL {

    /**
     * Version for Bengt Jonsson Festschrift
     *
     * Here only C-{x} is used instead of the sum operator
     *
     * @param machines
     * @param orders
     * @return
     */
    public static HLPetriGame generateImprovedVersionWithSetMinus(int machines, int orders) {
        HLPetriGame game = generateImprovedVersion(machines, orders);
        Transition d = game.getTransition("d");
        game.setPredicate(d, Constants.TRUE);
        Flow f = game.getFlow(d, game.getPlace("OK"));
        game.setArcExpression(f, new ArcExpression(new SetMinusTerm(new ColorClassTerm("M"), new Variable("m"))));
        return game;
    }

    /**
     * Version for Bengt Jonsson Festschrift
     *
     *      *
     * This version uses the sum operator instead of C-{x}.
     *
     * @param machines
     * @param orders
     * @return
     */
    public static HLPetriGame generateImprovedVersion(int machines, int orders) {
        if (machines < 2 || orders < 1) {
            throw new RuntimeException("less than 2 machines or 1 order does not make any sense!");
        }

        HLPetriGame net = new HLPetriGame("High-Level Concurrent Machines with " + machines + " machines and " + orders + " orders. (Safety)");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // create the color classes
        Color[] m = new Color[machines];
        for (int i = 0; i < m.length; i++) {
            m[i] = new Color("m" + i);
        }
        Color[] o = new Color[orders];
        for (int i = 0; i < o.length; i++) {
            o[i] = new Color("o" + i);
        }
        net.createBasicColorClass("M", false, m);
        net.createBasicColorClass("O", false, o);
        net.createBasicColorClass("E", false, "e");
        // color classes        
        String[] ec = {"E"};
        String[] mc = {"M"};
        String[] oc = {"O"};
        String[] omc = {"O", "M"};

        // Environment
        Place start = net.createEnvPlace("Env", ec);
        net.setColorTokens(start, "e");

        // activate/deactivate
        Place err = net.createSysPlace("ERR", mc);
        Place ok = net.createSysPlace("OK", mc);
        ArcExpression expr = new ArcExpression();
        Variable mVar = new Variable("m");
        List<IPredicate> uneq = new ArrayList<>();
        for (int i = 0; i < machines - 1; i++) {
            Variable mi = new Variable("m" + i);
            expr.add(mi);
            uneq.add(new BasicPredicate(mVar, BasicPredicate.Operator.NEQ, mi));
        }
        IPredicate p1 = Predicate.createPredicate(uneq, Predicate.Operator.AND);
        // this next predicate would not be needed if the check online valuations which correspond to safe nets                
        List<IPredicate> different = new ArrayList<>();
        for (int i = 0; i < machines - 1; i++) {
            Variable mi = new Variable("m" + i);
            for (int j = i + 1; j < machines - 1; j++) {
                Variable mj = new Variable("m" + j);
                different.add(new BasicPredicate(mi, BasicPredicate.Operator.NEQ, mj));
            }
        }
        IPredicate p2 = Predicate.createPredicate(different, Predicate.Operator.AND);
        Transition d = net.createTransition("d", new Predicate(p1, Predicate.Operator.AND, p2));
        net.createFlow(start, d, new ArcExpression(new Variable("e")));
        net.createFlow(d, err, new ArcExpression(new Variable("m")));
        net.createFlow(d, ok, expr);

        // testing
        Place s = net.createSysPlace("Sys", oc);
        net.setColorTokens(s, o);
        Transition testT = net.createTransition("test");
        net.createFlow(err, testT, new ArcExpression(mVar));
        net.createFlow(testT, s, new ArcExpression(new ColorClassTerm("O")));
        net.createFlow(s, testT, new ArcExpression(new ColorClassTerm("O")));

        // working
        ArcExpression omA = new ArcExpression(new ArcTuple(new Variable("o"), new Variable("m")));
        Place mPlace = net.createSysPlace("M", omc);
        Transition t = net.createTransition("p");
        net.createFlow(s, t, new ArcExpression(new Variable("o")));
        net.createFlow(t, mPlace, omA);

        Place bad = net.createSysPlace("B", omc);
        net.setBad(bad);
        t = net.createTransition("b");
        net.createFlow(mPlace, t, omA);
        net.createFlow(t, bad, omA);

        Place good = net.createSysPlace("G", omc);
        t = net.createTransition("g");
        net.createFlow(mPlace, t, omA);
        net.createFlow(ok, t, new ArcExpression(new Variable("m")));
        net.createFlow(t, good, omA);
        return net;
    }
}
