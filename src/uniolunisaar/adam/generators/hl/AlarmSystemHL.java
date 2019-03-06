package uniolunisaar.adam.generators.hl;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.predicate.BasicPredicate;
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
        String[] colors = new String[nb_alarmSystems];
        for (int i = 0; i < nb_alarmSystems; i++) {
            colors[i] = "a1";
        }
        net.createBasicColorClass("alarmsystems", false, colors);
        net.createBasicColorClass("burglar", false, "e");
        // color classes        
        String[] ec = {"burglar"};
        String[] s0 = {"alarmsystems"};
        String[] s1 = {"alarmsystems", "alarmsystems"};

        // Environment
        Place env = net.createEnvPlace("env", ec);
//        env.setInitialToken(1);
        Place env1 = net.createEnvPlace("C", ec);
        Transition t = net.createTransition("i");
        net.createFlow(env, t);
        net.createFlow(t, env1);
        Place e = net.createEnvPlace("I", ec);
        // system
        Place alarmSystem = net.createSysPlace("S", s0);
//        alarmSystem.setInitialToken(1);
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
        net.createFlow(alarmSystem, t);
        net.createFlow(t, initAlarm);
        Transition info = net.createTransition("info");
        net.createFlow(in, info);
        net.createFlow(info, initAlarm);
        // decide for alarm
        Place p = net.createSysPlace("Alarm", s1);
        t = net.createTransition();
        net.createFlow(initAlarm, t);
        net.createFlow(t, p);

        // flows for informing the other systems
        net.createFlow(alarmSystem, info);
        // how winning
        // create bad place
        Place error = net.createSysPlace("Bad", ec);
        net.setBad(error);
        // for the env places before EA, EB, etc
        Transition tr = net.createTransition("bot1");
        net.createFlow(env1, t);
        net.createFlow(t, error);
        net.createFlow(p, tr);
        // for the EA, EB, etc places
        tr = net.createTransition("bot2", new BasicPredicate(new Variable("b"), BasicPredicate.Operator.NEQ, new Variable("x")));
        net.createFlow(e, tr);
        net.createFlow(tr, error);
        net.createFlow(p, tr);

        // create good place
        Place good = net.createSysPlace("Good", ec);

        // create transitions
        t = net.createTransition("g");
        net.createFlow(e, t);
        net.createFlow(t, good);
        net.createFlow(p, t);
        return net;
    }

}
