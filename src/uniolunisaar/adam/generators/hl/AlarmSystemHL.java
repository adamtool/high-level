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
 * Creates the burglar examples for n alarm systems Corresponds to the low-level
 * version uniolunisaar.adam.generators.pg.SecuritySystem.
 *
 * @author Manuel Gieseking
 */
public class AlarmSystemHL {

    /**
     * Creates the example for the alarm system presented in the high-level
     * representation paper for the festschrift of Bengt Jonsson.
     *
     * Uses the new SetminusTerm for the arc expression
     *
     * @param nb_alarmSystems
     * @return
     */
    public static HLPetriGame createSafetyVersionForHLRepWithSetMinus(int nb_alarmSystems) {
        HLPetriGame game = createSafetyVersionForHLRep(nb_alarmSystems);
        Transition info = game.getTransition("info");
        game.setPredicate(info, Constants.TRUE);
        Flow f = game.getFlow(game.getPlace("S"), info);
        game.setArcExpression(f, new ArcExpression(new SetMinusTerm(new ColorClassTerm("alarmsystems"), new Variable("x"))));
        return game;
    }

    /**
     * Creates the example for the alarm system presented in the high-level
     * representation paper for the festschrift of Bengt Jonsson.
     *
     * This version uses the sum operator instead of C-{x}.
     *
     * @param nb_alarmSystems
     * @return
     */
    public static HLPetriGame createSafetyVersionForHLRep(int nb_alarmSystems) {
        if (nb_alarmSystems < 2) {
            throw new RuntimeException("less than 2 intruding points are not "
                    + "interesting for a security system");
        }
        HLPetriGame net = new HLPetriGame("High-Level Alarm system with " + nb_alarmSystems + " intruding points. (Safety)");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // create the color classes
        Color[] colors = new Color[nb_alarmSystems];
        for (int i = 0; i < nb_alarmSystems; i++) {
            colors[i] = new Color("a" + i);
        }
        net.createBasicColorClass("alarmsystems", false, colors);
        net.createBasicColorClass("burglar", false, "e");
        // color classes        
        String[] ec = {"burglar"};
        String[] s0 = {"alarmsystems"};
        String[] s1 = {"alarmsystems", "alarmsystems"};

        // Environment
        Place env = net.createEnvPlace("env", ec);
        net.setColorTokens(env, "e");
        Place env1 = net.createEnvPlace("C", s0);
        Transition t = net.createTransition("i");
        net.createFlow(env, t, new ArcExpression(new Variable("e")));
        net.createFlow(t, env1);
        Place e = net.createEnvPlace("I", s0);
        // system
        Place alarmSystem = net.createSysPlace("S", s0);
        net.setColorTokens(alarmSystem, colors);
        Place in = net.createSysPlace("D", s0);
        Place initAlarm = net.createSysPlace("P", s0);

        t = net.createTransition("t");
        net.createFlow(env1, t);
        net.createFlow(alarmSystem, t);
        net.createFlow(t, e);
        net.createFlow(t, in);

        t = net.createTransition("fr");
        net.createFlow(in, t);
        net.createFlow(t, initAlarm);

        t = net.createTransition("fa");
        net.createFlow(alarmSystem, t, new ArcExpression(new Variable("y")));
        net.createFlow(t, initAlarm, new ArcExpression(new Variable("y")));

        ArcExpression expr = new ArcExpression();
        Variable x = new Variable("x");
        List<IPredicate> uneq = new ArrayList<>();
        for (int i = 0; i < nb_alarmSystems - 1; i++) {
            Variable xi = new Variable("x" + i);
            expr.add(xi);
            uneq.add(new BasicPredicate(x, BasicPredicate.Operator.NEQ, xi));
        }
        IPredicate p1 = Predicate.createPredicate(uneq, Predicate.Operator.AND);
        // this next predicate would not be needed if the check online valuations which correspond to safe nets                
        List<IPredicate> different = new ArrayList<>();
        for (int i = 0; i < nb_alarmSystems - 1; i++) {
            Variable xi = new Variable("x" + i);
            for (int j = i + 1; j < nb_alarmSystems - 1; j++) {
                Variable xj = new Variable("x" + j);
                different.add(new BasicPredicate(xi, BasicPredicate.Operator.NEQ, xj));
            }
        }
        IPredicate p2 = Predicate.createPredicate(different, Predicate.Operator.AND);
        Transition info = net.createTransition("info", new Predicate(p1, Predicate.Operator.AND, p2));
        net.createFlow(in, info, new ArcExpression(x));
        net.createFlow(info, initAlarm, new ArcExpression(new ColorClassTerm("alarmsystems")));
        net.createFlow(alarmSystem, info, expr);

        // decide for alarm
        Place p = net.createSysPlace("Alarm", s1);
        t = net.createTransition();
        net.createFlow(initAlarm, t, new ArcExpression(new Variable("z")));
        net.createFlow(t, p, new ArcExpression(new ArcTuple(new Variable("z"), new Variable("v"))));

        // how winning
        // create bad place
        Place error = net.createSysPlace("Bad", ec);
        net.setBad(error);
        // for the env places before EA, EB, etc
        Transition tr = net.createTransition("bot1");
        net.createFlow(env1, tr);
        net.createFlow(tr, error, new ArcExpression(new Variable("e")));
        net.createFlow(p, tr, new ArcExpression(new ArcTuple(new Variable("a"), new Variable("b"))));
        // for the EA, EB, etc places
        tr = net.createTransition("bot2", new BasicPredicate<>(new Variable("b"), BasicPredicate.Operator.NEQ, new Variable("x")));
        net.createFlow(e, tr);
        net.createFlow(tr, error, new ArcExpression(new Variable("e")));
        net.createFlow(p, tr, new ArcExpression(new ArcTuple(new Variable("a"), new Variable("b"))));

        // create good place
        Place good = net.createSysPlace("Good", ec);

        // create transitions
        t = net.createTransition("g");
        net.createFlow(e, t);
        net.createFlow(t, good, new ArcExpression(new Variable("e")));
        ArcTuple tuple = new ArcTuple();
        tuple.add(new ColorClassTerm("alarmsystems"));
        tuple.add(x);
        net.createFlow(p, t, new ArcExpression(tuple));
        return net;
    }

}
