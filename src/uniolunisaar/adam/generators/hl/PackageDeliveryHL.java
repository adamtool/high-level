package uniolunisaar.adam.generators.hl;

import java.util.HashMap;
import java.util.Map;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.arcexpressions.SetMinusTerm;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class PackageDeliveryHL {
    
        /**
     *
     * Variante with emergency signal (not yet implemented)
     * 
     * @param withPartition
     * @return
     */
    public static HLPetriGame generateD(int nb_drones, int nb_packages, boolean withPartition) {
//        if (machines < 2 || orders < 1) {
//            throw new RuntimeException("less than 2 machines or 1 order does not make any sense!");
//        }

        HLPetriGame net = new HLPetriGame("High-Level Package Delivery");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        if (withPartition) {
            Map<String, Integer> partitions = new HashMap<>();
            for (int i = 0; i < nb_drones; i++) {
//                partitions.put("drones_d" + i, i + 1);
                partitions.put("fin_d" + i, i + 1);
//                partitions.put("ERR_d" + i, nb_drones + 1);
                partitions.put("OK_d" + i, i + 1);
                for (int j = 0; j < nb_packages; j++) {
                    partitions.put("fly_d" + i + "xp" + j, nb_drones + j + 1);
                }
            }

            for (int i = 0; i < nb_packages; i++) {
                partitions.put("pack_p" + i, nb_drones + 1 + i);
                partitions.put("lost_p" + i, nb_drones + 1 + i);
                partitions.put("goal_p" + i, nb_drones + 1 + i);
                partitions.put("bad_p" + i, nb_drones + 1 + i);
            }
            net.putExtension("partitions", partitions);
        }
        // create the color classes
        Color[] drones = new Color[nb_drones];
        for (int i = 0; i < drones.length; i++) {
            drones[i] = new Color("d" + i);
        }
        Color[] packages = new Color[nb_packages];
        for (int i = 0; i < packages.length; i++) {
            packages[i] = new Color("p" + i);
        }
        net.createBasicColorClass("D", false, drones);
        net.createBasicColorClass("P", false, packages);
        net.createBasicColorClass("E", false, "e");
        // color classes        
        String[] ec = {"E"};
        String[] dc = {"D"};
        String[] pc = {"P"};
        String[] dp = {"D", "P"};

        // Environment
        Place start = net.createEnvPlace("Env", ec);
        net.setColorTokens(start, "e");

        // destroy drones
        Place err = net.createEnvPlace("ERR", dc);
        Place ok = net.createSysPlace("OK", dc);
        Variable dVar = new Variable("d");
        Variable pVar = new Variable("p");
        ArcExpression dArc = new ArcExpression(dVar);
        ArcExpression pArc = new ArcExpression(pVar);
        ArcExpression tuple = new ArcExpression(new ArcTuple(dVar, pVar));
        Transition d = net.createTransition("des");
        net.createFlow(start, d, new ArcExpression(new Variable("e")));
        net.createFlow(d, err, new ArcExpression(dVar));
        net.createFlow(d, ok, new ArcExpression(new SetMinusTerm(new ColorClassTerm("D"), dVar)));

        // drones 
//        Place dr = net.createSysPlace("drones", dc); // variante A
//        net.setColorTokens(dr, drones);
        // packages 
        Place pack = net.createSysPlace("pack", pc);
        net.setColorTokens(pack, packages);

        // flying        
        Place fly = net.createSysPlace("fly", dp);
        Transition t = net.createTransition("t0");
//        net.createFlow(dr, t, dArc); // variante A
        net.createFlow(pack, t, pArc);
        net.createFlow(t, fly, tuple);

        Place lost = net.createSysPlace("lost", pc);
        t = net.createTransition("t1");
        net.createFlow(err, t, dArc);
        net.createFlow(fly, t, tuple);
        net.createFlow(t, lost, pArc);
        net.createFlow(t, err, dArc);

        Place fin = net.createSysPlace("fin", dc);
        Place goal = net.createSysPlace("goal", pc);
        t = net.createTransition("t2");
        net.createFlow(fly, t, tuple);
        net.createFlow(ok, t, dArc);
//        net.createFlow(t, ok, dArc); // Variante A
        net.createFlow(t, fin, dArc);
        net.createFlow(t, goal, pArc);

        t = net.createTransition("t3");
        net.createFlow(lost, t, pArc);
        net.createFlow(fin, t, dArc);
        net.createFlow(t, goal, pArc); // VARIANTE B
//        net.createFlow(t, fly, tuple); // VARIANTE A

        Place bad = net.createSysPlace("bad", pc);
        net.setBad(bad);
        t = net.createTransition("t4");
        net.createFlow(goal, t, pArc);
        net.createFlow(t, bad, pArc);

        Place end = net.createEnvPlace("end", ec);
        t = net.createTransition("t5");
        net.createFlow(goal, t, new ArcExpression(new ColorClassTerm("P")));
        net.createFlow(t, end, new ArcExpression(new Variable("e")));
        net.createFlow(err, t, new ArcExpression(new Variable("d")));
        
//        t = net.createTransition("t6");
//        net.createFlow(fly, t, tuple);
//        net.createFlow(t, bad, pArc);
//        
        t = net.createTransition("t7");
        net.createFlow(lost, t, pArc);
        net.createFlow(t, bad, pArc);

        return net;
    }
    

    /**
     *
     * @param withPartition
     * @return
     */
    public static HLPetriGame generateB(int nb_drones, int nb_packages, boolean withPartition) {
//        if (machines < 2 || orders < 1) {
//            throw new RuntimeException("less than 2 machines or 1 order does not make any sense!");
//        }

        HLPetriGame net = new HLPetriGame("High-Level Package Delivery");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        if (withPartition) {
            Map<String, Integer> partitions = new HashMap<>();
            for (int i = 0; i < nb_drones; i++) {
//                partitions.put("drones_d" + i, i + 1);
                partitions.put("fin_d" + i, i + 1);
//                partitions.put("ERR_d" + i, nb_drones + 1);
                partitions.put("OK_d" + i, i + 1);
                for (int j = 0; j < nb_packages; j++) {
                    partitions.put("fly_d" + i + "xp" + j, nb_drones + j + 1);
                }
            }

            for (int i = 0; i < nb_packages; i++) {
                partitions.put("pack_p" + i, nb_drones + 1 + i);
                partitions.put("lost_p" + i, nb_drones + 1 + i);
                partitions.put("goal_p" + i, nb_drones + 1 + i);
                partitions.put("bad_p" + i, nb_drones + 1 + i);
            }
            net.putExtension("partitions", partitions);
        }
        // create the color classes
        Color[] drones = new Color[nb_drones];
        for (int i = 0; i < drones.length; i++) {
            drones[i] = new Color("d" + i);
        }
        Color[] packages = new Color[nb_packages];
        for (int i = 0; i < packages.length; i++) {
            packages[i] = new Color("p" + i);
        }
        net.createBasicColorClass("D", false, drones);
        net.createBasicColorClass("P", false, packages);
        net.createBasicColorClass("E", false, "e");
        // color classes        
        String[] ec = {"E"};
        String[] dc = {"D"};
        String[] pc = {"P"};
        String[] dp = {"D", "P"};

        // Environment
        Place start = net.createEnvPlace("Env", ec);
        net.setColorTokens(start, "e");

        // destroy drones
        Place err = net.createEnvPlace("ERR", dc);
        Place ok = net.createSysPlace("OK", dc);
        Variable dVar = new Variable("d");
        Variable pVar = new Variable("p");
        ArcExpression dArc = new ArcExpression(dVar);
        ArcExpression pArc = new ArcExpression(pVar);
        ArcExpression tuple = new ArcExpression(new ArcTuple(dVar, pVar));
        Transition d = net.createTransition("des");
        net.createFlow(start, d, new ArcExpression(new Variable("e")));
        net.createFlow(d, err, new ArcExpression(dVar));
        net.createFlow(d, ok, new ArcExpression(new SetMinusTerm(new ColorClassTerm("D"), dVar)));

        // drones 
//        Place dr = net.createSysPlace("drones", dc); // variante A
//        net.setColorTokens(dr, drones);
        // packages 
        Place pack = net.createSysPlace("pack", pc);
        net.setColorTokens(pack, packages);

        // flying        
        Place fly = net.createSysPlace("fly", dp);
        Transition t = net.createTransition("t0");
//        net.createFlow(dr, t, dArc); // variante A
        net.createFlow(pack, t, pArc);
        net.createFlow(t, fly, tuple);

        Place lost = net.createSysPlace("lost", pc);
        t = net.createTransition("t1");
        net.createFlow(err, t, dArc);
        net.createFlow(fly, t, tuple);
        net.createFlow(t, lost, pArc);
        net.createFlow(t, err, dArc);

        Place fin = net.createSysPlace("fin", dc);
        Place goal = net.createSysPlace("goal", pc);
        t = net.createTransition("t2");
        net.createFlow(fly, t, tuple);
        net.createFlow(ok, t, dArc);
//        net.createFlow(t, ok, dArc); // Variante A
        net.createFlow(t, fin, dArc);
        net.createFlow(t, goal, pArc);

        t = net.createTransition("t3");
        net.createFlow(lost, t, pArc);
        net.createFlow(fin, t, dArc);
        net.createFlow(t, goal, pArc); // VARIANTE B
//        net.createFlow(t, fly, tuple); // VARIANTE A

        Place bad = net.createSysPlace("bad", pc);
        net.setBad(bad);
        t = net.createTransition("t4");
        net.createFlow(goal, t, pArc);
        net.createFlow(t, bad, pArc);

        Place end = net.createEnvPlace("end", ec);
        t = net.createTransition("t5");
        net.createFlow(goal, t, new ArcExpression(new ColorClassTerm("P")));
        net.createFlow(t, end, new ArcExpression(new Variable("e")));
        net.createFlow(err, t, new ArcExpression(new Variable("d")));
        
//        t = net.createTransition("t6");
//        net.createFlow(fly, t, tuple);
//        net.createFlow(t, bad, pArc);
//        
        t = net.createTransition("t7");
        net.createFlow(lost, t, pArc);
        net.createFlow(t, bad, pArc);

        return net;
    }

    /**
     * Version for Bengt Jonsson Festschrift
     *
     * more realistic but more expensive
     *
     * This version uses the sum operator instead of C-{x}.
     *
     * @param withPartition
     * @return
     */
    public static HLPetriGame generateA(int nb_drones, int nb_packages, boolean withPartition) {
//        if (machines < 2 || orders < 1) {
//            throw new RuntimeException("less than 2 machines or 1 order does not make any sense!");
//        }

        HLPetriGame net = new HLPetriGame("High-Level Package Delivery");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        if (withPartition) {
            Map<String, Integer> partitions = new HashMap<>();
            for (int i = 0; i < nb_drones; i++) {
                partitions.put("drones_d" + i, i + 1);
                partitions.put("fin_d" + i, i + 1);
//                partitions.put("ERR_d" + i, nb_drones + 1);
                partitions.put("OK_d" + i, nb_drones + 1 + i);
                for (int j = 0; j < nb_packages; j++) {
                    partitions.put("fly_d" + i + "xp" + j, i + 1);
                }
            }

            for (int i = 0; i < nb_packages; i++) {
                partitions.put("pack_p" + i, 2 * nb_drones + 1 + i);
                partitions.put("lost_p" + i, 2 * nb_drones + 1 + i);
                partitions.put("goal_p" + i, 2 * nb_drones + 1 + i);
                partitions.put("bad_p" + i, 2 * nb_drones + 1 + i);
            }
            net.putExtension("partitions", partitions);
        }
        // create the color classes
        Color[] drones = new Color[nb_drones];
        for (int i = 0; i < drones.length; i++) {
            drones[i] = new Color("d" + i);
        }
        Color[] packages = new Color[nb_packages];
        for (int i = 0; i < packages.length; i++) {
            packages[i] = new Color("p" + i);
        }
        net.createBasicColorClass("D", false, drones);
        net.createBasicColorClass("P", false, packages);
        net.createBasicColorClass("E", false, "e");
        // color classes        
        String[] ec = {"E"};
        String[] dc = {"D"};
        String[] pc = {"P"};
        String[] dp = {"D", "P"};

        // Environment
        Place start = net.createEnvPlace("Env", ec);
        net.setColorTokens(start, "e");

        // destroy drones
        Place err = net.createEnvPlace("ERR", dc);
        Place ok = net.createSysPlace("OK", dc);
        Variable dVar = new Variable("d");
        Variable pVar = new Variable("p");
        ArcExpression dArc = new ArcExpression(dVar);
        ArcExpression pArc = new ArcExpression(pVar);
        ArcExpression tuple = new ArcExpression(new ArcTuple(dVar, pVar));
        Transition d = net.createTransition("des");
        net.createFlow(start, d, new ArcExpression(new Variable("e")));
        net.createFlow(d, err, new ArcExpression(dVar));
        net.createFlow(d, ok, new ArcExpression(new SetMinusTerm(new ColorClassTerm("D"), dVar)));

        // drones 
        Place dr = net.createSysPlace("drones", dc);
        net.setColorTokens(dr, drones);
        // packages 
        Place pack = net.createSysPlace("pack", pc);
        net.setColorTokens(pack, packages);

        // flying        
        Place fly = net.createSysPlace("fly", dp);
        Transition t = net.createTransition();
        net.createFlow(dr, t, dArc);
        net.createFlow(pack, t, pArc);
        net.createFlow(t, fly, tuple);

        Place lost = net.createSysPlace("lost", pc);
        t = net.createTransition();
        net.createFlow(err, t, dArc);
        net.createFlow(fly, t, tuple);
        net.createFlow(t, lost, pArc);

        Place fin = net.createSysPlace("fin", dc);
        Place goal = net.createSysPlace("goal", pc);
        t = net.createTransition();
        net.createFlow(fly, t, tuple);
        net.createFlow(ok, t, dArc);
        net.createFlow(t, ok, dArc);
        net.createFlow(t, fin, dArc);
        net.createFlow(t, goal, pArc);

        t = net.createTransition();
        net.createFlow(lost, t, pArc);
        net.createFlow(fin, t, dArc);
        net.createFlow(t, fly, tuple);

        Place bad = net.createSysPlace("bad", pc);
        net.setBad(bad);
        t = net.createTransition();
        net.createFlow(goal, t, pArc);
        net.createFlow(t, bad, pArc);

        Place end = net.createEnvPlace("end", ec);
        t = net.createTransition();
        net.createFlow(goal, t, new ArcExpression(new ColorClassTerm("P")));
        net.createFlow(t, end, new ArcExpression(new Variable("e")));

        return net;
    }
}
