package uniolunisaar.adam.generators.hl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.predicate.BasicPredicate;
import uniolunisaar.adam.ds.highlevel.predicate.DomainTerm;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class ContainerHabourHL {

    /**
     *
     * @param size
     * @param withPartition
     * @return
     */
    public static HLPetriGame generateD(int nb_container, int nb_ct_places, int nb_robots, int nb_costs, boolean withPartition) {
//        if (size < 1) {
//            throw new RuntimeException("There should at least be one Clerk to sign the document.");
//        }
        HLPetriGame net = new HLPetriGame("High-Level ");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        if (withPartition) {
            Map<String, Integer> partitions = new HashMap<>();
            for (int i = 0; i < nb_container; i++) {
                partitions.put("eC_c" + i, nb_container + 1 + i);
                for (int j = 0; j < nb_ct_places; j++) {
                    partitions.put("rec_c" + i + "xcp" + j, nb_container + 1 + i);
                }
                partitions.put("ship_c" + i, i + 1);
                partitions.put("sC_c" + i, i + 1);
                partitions.put("bad_c" + i, i + 1);
                for (int j = 0; j < nb_ct_places; j++) {
                    partitions.put("conP_c" + i + "xcp" + j, i + 1);
                }
            }
            int actToken = nb_container + nb_container + 1;
            for (int i = 0; i < nb_robots; i++) {
                partitions.put("Rob_r" + i + "xdead", actToken + i);
                partitions.put("Rob_r" + i + "xlow", actToken + i);
                partitions.put("Rob_r" + i + "xmedium", actToken + i);
                partitions.put("Rob_r" + i + "xhigh", actToken + i);
                partitions.put("scanned_r" + i + "xdead", actToken + i);
                partitions.put("scanned_r" + i + "xlow", actToken + i);
                partitions.put("scanned_r" + i + "xmedium", actToken + i);
                partitions.put("scanned_r" + i + "xhigh", actToken + i);
            }

//            for (int i = 0; i < nb_costs; i++) {
//                partitions.put("costs_m" + i, actToken + nb_robots + i);
//            }

            net.putExtension("partitions", partitions);
        }
        // create the color classes
        Color[] cont = new Color[nb_container];
        for (int i = 0; i < cont.length; i++) {
            cont[i] = new Color("c" + i);
        }
        Color[] cp = new Color[nb_ct_places];
        for (int i = 0; i < cp.length; i++) {
            cp[i] = new Color("cp" + i);
        }
        Color[] robots = new Color[nb_robots];
        for (int i = 0; i < robots.length; i++) {
            robots[i] = new Color("r" + i);
        }
        Color[] costs = new Color[nb_costs];
        for (int i = 0; i < costs.length; i++) {
            costs[i] = new Color("m" + i);
        }

        List<Pair<String, String[]>> batteries = new ArrayList<>();
        batteries.add(new Pair<>("B0", new String[]{"dead"}));
//        batteries.add(new Pair<>("B1", new String[]{"low"}));
//        batteries.add(new Pair<>("B2", new String[]{"medium"}));
        batteries.add(new Pair<>("B3", new String[]{"high"}));

        net.createBasicColorClass("E", false, "e");
        net.createBasicColorClass("C", false, cont);
        net.createBasicColorClass("CP", false, cp);
        net.createBasicColorClass("R", false, robots);
        net.createBasicColorClassByStaticSubClass("B", true, batteries);
        net.createBasicColorClass("K", false, costs);

        // possible color domains
        String[] ec = {"E"};
        String[] c = {"C"};
        String[] p = {"CP"};
        String[] ccp = {"C", "CP"};
        String[] rb = {"R", "B"};
        String[] k = {"K"};

        Place e0 = net.createEnvPlace("e0", ec);
        net.setColorTokens(e0, "e");
        Place e1 = net.createEnvPlace("e1", p);
        Transition t = net.createTransition();
        net.createFlow(e0, t);
        net.createFlow(t, e1, new ArcExpression(new Variable("p")));

        // tagging
        Place eC = net.createSysPlace("eC", c);
        net.setColorTokens(eC, cont);
        Place rec = net.createSysPlace("rec", ccp);
        t = net.createTransition();
        net.createFlow(e1, t, new ArcExpression(new Variable("p")));
        net.createFlow(eC, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, rec, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(t, e0);

        // ship
        Place ship = net.createSysPlace("ship", c);
        net.setColorTokens(ship, cont);
        Place sC = net.createSysPlace("sC", c);
        t = net.createTransition();
        net.createFlow(ship, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, sC, new ArcExpression(new Variable("c")));
        t = net.createTransition();
        net.createFlow(ship, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, sC, new ArcExpression(new Variable("c")));
        net.createFlow(t, rec, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(rec, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

        // container place
        Place contP = net.createSysPlace("conP", ccp);
        Place bad = net.createSysPlace("bad", c);
        net.setBad(bad);
        t = net.createTransition();
        net.createFlow(contP, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(t, bad, new ArcExpression(new Variable("c")));        
        t = net.createTransition();
        net.createFlow(sC, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, bad, new ArcExpression(new Variable("c")));
        t = net.createTransition();
        net.createFlow(rec, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(contP, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

        // costs
//        Place ccost = net.createSysPlace("costs", k);
//        net.setColorTokens(ccost, costs);
        // scanning 
        Place scanned = net.createSysPlace("scanned", rb);

        // robot
        Place rob = net.createSysPlace("Rob", rb);
        List<ColorToken> init = new ArrayList<>();
        for (int i = 0; i < nb_robots; i++) {
            init.add(new ColorToken(new Color("r" + i), new Color("high")));
        }
        net.setColorTokens(rob, init);
        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.NEQ, new ColorClassTerm("B3")));
        net.createFlow(sC, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, sC, new ArcExpression(new Variable("c")));
        ArcTuple tup = new ArcTuple();
        tup.add(new Variable("r"));
        tup.add(new SuccessorTerm(new Variable("b"), net));
        net.createFlow(rob, t, new ArcExpression(tup));
//        net.createFlow(t, rob, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b")))); // VERSION A
//        net.createFlow(t, contP, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(t, scanned, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b")))); // VERSION D

        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.NEQ, new ColorClassTerm("B3")));
        net.createFlow(rob, t, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
//        net.createFlow(ccost, t, new ArcExpression(new Variable("z")));
        net.createFlow(t, rob, new ArcExpression(tup));
//        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.EQ, new ColorClassTerm("B3")));
//        net.createFlow(rob, t, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
//        net.createFlow(ccost, t, new ArcExpression(new Variable("z")));
//        net.createFlow(t, rob, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        // ADDED for VERSION D
        t = net.createTransition();
        net.createFlow(scanned, t, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(sC, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, rob, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(t, contP, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

        return net;
    }

    /**
     * Not functioning version where there is a dump place which should solve
     * the non-determinism. Currently not sure why this is not working.
     * Additionally the part added by B where the tagging is restricted to one
     * should also be deletable.
     *
     * @param size
     * @param withPartition
     * @return
     */
    public static HLPetriGame generateC(int nb_container, int nb_ct_places, int nb_robots, int nb_costs, int nb_dumps, boolean withPartition) {
//        if (size < 1) {
//            throw new RuntimeException("There should at least be one Clerk to sign the document.");
//        }
        HLPetriGame net = new HLPetriGame("High-Level ");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        if (withPartition) {
            Map<String, Integer> partitions = new HashMap<>();
            for (int i = 0; i < nb_container; i++) {
                partitions.put("eC_c" + i, nb_container + 1 + i);
                for (int j = 0; j < nb_ct_places; j++) {
                    partitions.put("rec_c" + i + "xcp" + j, nb_container + 1 + i);
                    partitions.put("conP_c" + i + "xcp" + j, i + 1);
                }
                partitions.put("ship_c" + i, i + 1);
                partitions.put("bad_c" + i, i + 1);
                for (int j = 0; j < nb_dumps; j++) {
                    partitions.put("sC_c" + i + "xd" + j, i + 1);
                }
            }
            int actToken = nb_container + nb_container + 1;
            for (int i = 0; i < nb_robots; i++) {
                partitions.put("Rob_r" + i + "xdead", actToken + i);
                partitions.put("Rob_r" + i + "xlow", actToken + i);
                partitions.put("Rob_r" + i + "xmedium", actToken + i);
                partitions.put("Rob_r" + i + "xhigh", actToken + i);
            }

            for (int i = 0; i < nb_costs; i++) {
                partitions.put("costs_m" + i, actToken + nb_robots + i);
            }

            for (int i = 0; i < nb_dumps; i++) {
                partitions.put("dump_d" + i, actToken + nb_robots + nb_costs + i);
            }

            net.putExtension("partitions", partitions);
        }
        // create the color classes
        Color[] cont = new Color[nb_container];
        for (int i = 0; i < cont.length; i++) {
            cont[i] = new Color("c" + i);
        }
        Color[] cp = new Color[nb_ct_places];
        for (int i = 0; i < cp.length; i++) {
            cp[i] = new Color("cp" + i);
        }
        Color[] robots = new Color[nb_robots];
        for (int i = 0; i < robots.length; i++) {
            robots[i] = new Color("r" + i);
        }
        Color[] costs = new Color[nb_costs];
        for (int i = 0; i < costs.length; i++) {
            costs[i] = new Color("m" + i);
        }
        Color[] dumps = new Color[nb_dumps];
        for (int i = 0; i < dumps.length; i++) {
            dumps[i] = new Color("d" + i);
        }

        List<Pair<String, String[]>> batteries = new ArrayList<>();
        batteries.add(new Pair<>("B0", new String[]{"dead"}));
        batteries.add(new Pair<>("B1", new String[]{"low"}));
        batteries.add(new Pair<>("B2", new String[]{"medium"}));
        batteries.add(new Pair<>("B3", new String[]{"high"}));

        net.createBasicColorClass("E", false, "e");
        net.createBasicColorClass("C", false, cont);
        net.createBasicColorClass("CP", false, cp);
        net.createBasicColorClass("R", false, robots);
        net.createBasicColorClassByStaticSubClass("B", true, batteries);
        net.createBasicColorClass("K", false, costs);
        net.createBasicColorClass("D", false, dumps);

        // possible color domains
        String[] ec = {"E"};
        String[] c = {"C"};
        String[] p = {"CP"};
        String[] ccp = {"C", "CP"};
        String[] cd = {"C", "D"};
        String[] rb = {"R", "B"};
        String[] k = {"K"};
        String[] d = {"D"};

        Place e0 = net.createEnvPlace("e0", ec);
        net.setColorTokens(e0, "e");
        Place e1 = net.createEnvPlace("e1", p);
        Transition t = net.createTransition();
        net.createFlow(e0, t);
        net.createFlow(t, e1, new ArcExpression(new Variable("p")));

        // tagging
        Place eC = net.createSysPlace("eC", c);
        net.setColorTokens(eC, cont);
        Place rec = net.createSysPlace("rec", ccp);
        t = net.createTransition();
        net.createFlow(e1, t, new ArcExpression(new Variable("p")));
        net.createFlow(eC, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, rec, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

//        net.createFlow(t, e0);        // from version A
// next two line added for version B
        Place e2 = net.createEnvPlace("e2", ec);
        net.createFlow(t, e2);

        // new for version C the dumping ground
        Place dump = net.createSysPlace("dump", d);
        net.setColorTokens(dump, dumps);

        // ship
        Place ship = net.createSysPlace("ship", c);
        net.setColorTokens(ship, cont);
        Place sC = net.createSysPlace("sC", cd); // VERSION C
        t = net.createTransition();
        net.createFlow(ship, t, new ArcExpression(new Variable("c")));
////        net.createFlow(t, sC, new ArcExpression(new Variable("c"))); VERSION A
        net.createFlow(t, sC, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("d"))));// VERSION C
        net.createFlow(dump, t, new ArcExpression(new Variable("d")));
        t = net.createTransition();
        net.createFlow(ship, t, new ArcExpression(new Variable("c")));
//        net.createFlow(t, sC, new ArcExpression(new Variable("c"))); //VERSION A
        net.createFlow(t, sC, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("d"))));// VERSION C
        net.createFlow(t, rec, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(rec, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(dump, t, new ArcExpression(new Variable("d")));// VERSION C

        // container place
        Place contP = net.createSysPlace("conP", ccp);
        Place bad = net.createSysPlace("bad", c);
        net.setBad(bad);
        t = net.createTransition();
        net.createFlow(contP, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(t, bad, new ArcExpression(new Variable("c")));
        t = net.createTransition();
        net.createFlow(rec, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(contP, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        // next two lines added for Version B
        net.createFlow(e2, t);
        net.createFlow(t, e0);

        // costs
        Place ccost = net.createSysPlace("costs", k);
        net.setColorTokens(ccost, costs);

        // robot
        Place rob = net.createSysPlace("Rob", rb);
        List<ColorToken> init = new ArrayList<>();
        for (int i = 0; i < nb_robots; i++) {
            init.add(new ColorToken(new Color("r" + i), new Color("high")));
        }
        net.setColorTokens(rob, init);
        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.NEQ, new ColorClassTerm("B3")));
//        net.createFlow(sC, t, new ArcExpression(new Variable("c"))); // VERSION A
        net.createFlow(sC, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("d"))));// VERSION C
        ArcTuple tup = new ArcTuple();
        tup.add(new Variable("r"));
        tup.add(new SuccessorTerm(new Variable("b"), net));
        net.createFlow(rob, t, new ArcExpression(tup));
        net.createFlow(t, rob, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(t, contP, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(t, dump, new ArcExpression(new Variable("d")));// Version C

        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.NEQ, new ColorClassTerm("B3")));
        net.createFlow(rob, t, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(ccost, t, new ArcExpression(new Variable("z")));
        net.createFlow(t, rob, new ArcExpression(tup));

        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.EQ, new ColorClassTerm("B3")));
        net.createFlow(rob, t, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(ccost, t, new ArcExpression(new Variable("z")));
        net.createFlow(t, rob, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));

        return net;
    }

    /**
     * Functioning version (tested for 2 2 1 1 and 2 2 2 1) but the container
     * cannot leave the ship without getting tagged. And every container is
     * processed sequentially.
     *
     * @param size
     * @param withPartition
     * @return
     */
    public static HLPetriGame generateB(int nb_container, int nb_ct_places, int nb_robots, int nb_costs, boolean withPartition) {
//        if (size < 1) {
//            throw new RuntimeException("There should at least be one Clerk to sign the document.");
//        }
        HLPetriGame net = new HLPetriGame("High-Level ");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        if (withPartition) {
            Map<String, Integer> partitions = new HashMap<>();
            for (int i = 0; i < nb_container; i++) {
                partitions.put("eC_c" + i, nb_container + 1 + i);
                for (int j = 0; j < nb_ct_places; j++) {
                    partitions.put("rec_c" + i + "xcp" + j, nb_container + 1 + i);
                    partitions.put("sC_c" + i + "xcp" + j, i + 1);
                }
                partitions.put("ship_c" + i, i + 1);
                partitions.put("bad_c" + i, i + 1);
                for (int j = 0; j < nb_ct_places; j++) {
                    partitions.put("conP_c" + i + "xcp" + j, i + 1);
                }
            }
            int actToken = nb_container + nb_container + 1;
            for (int i = 0; i < nb_robots; i++) {
                partitions.put("Rob_r" + i + "xdead", actToken + i);
                partitions.put("Rob_r" + i + "xlow", actToken + i);
                partitions.put("Rob_r" + i + "xmedium", actToken + i);
                partitions.put("Rob_r" + i + "xhigh", actToken + i);
            }

            for (int i = 0; i < nb_costs; i++) {
                partitions.put("costs_m" + i, actToken + nb_robots + i);
            }

            net.putExtension("partitions", partitions);
        }
        // create the color classes
        Color[] cont = new Color[nb_container];
        for (int i = 0; i < cont.length; i++) {
            cont[i] = new Color("c" + i);
        }
        Color[] cp = new Color[nb_ct_places];
        for (int i = 0; i < cp.length; i++) {
            cp[i] = new Color("cp" + i);
        }
        Color[] robots = new Color[nb_robots];
        for (int i = 0; i < robots.length; i++) {
            robots[i] = new Color("r" + i);
        }
        Color[] costs = new Color[nb_costs];
        for (int i = 0; i < costs.length; i++) {
            costs[i] = new Color("m" + i);
        }

        List<Pair<String, String[]>> batteries = new ArrayList<>();
        batteries.add(new Pair<>("B0", new String[]{"dead"}));
        batteries.add(new Pair<>("B1", new String[]{"low"}));
        batteries.add(new Pair<>("B2", new String[]{"medium"}));
        batteries.add(new Pair<>("B3", new String[]{"high"}));

        net.createBasicColorClass("E", false, "e");
        net.createBasicColorClass("C", false, cont);
        net.createBasicColorClass("CP", false, cp);
        net.createBasicColorClass("R", false, robots);
        net.createBasicColorClassByStaticSubClass("B", true, batteries);
        net.createBasicColorClass("K", false, costs);

        // possible color domains
        String[] ec = {"E"};
        String[] c = {"C"};
        String[] p = {"CP"};
        String[] ccp = {"C", "CP"};
        String[] rb = {"R", "B"};
        String[] k = {"K"};

        Place e0 = net.createEnvPlace("e0", ec);
        net.setColorTokens(e0, "e");
        Place e1 = net.createEnvPlace("e1", p);
        Transition t = net.createTransition();
        net.createFlow(e0, t);
        net.createFlow(t, e1, new ArcExpression(new Variable("p")));

        // tagging
        Place eC = net.createSysPlace("eC", c);
        net.setColorTokens(eC, cont);
        Place rec = net.createSysPlace("rec", ccp);
        t = net.createTransition();
        net.createFlow(e1, t, new ArcExpression(new Variable("p")));
        net.createFlow(eC, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, rec, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

//        net.createFlow(t, e0);        // from version A
// next two line added for version B
        Place e2 = net.createEnvPlace("e2", ec);
        net.createFlow(t, e2);

        // ship
        Place ship = net.createSysPlace("ship", c);
        net.setColorTokens(ship, cont);
//        Place sC = net.createSysPlace("sC", c); // VERSION A
        Place sC = net.createSysPlace("sC", ccp); // VERSION B
        // different to version A, the container cannot leave the ship without ticket
//        t = net.createTransition();
//        net.createFlow(ship, t, new ArcExpression(new Variable("c")));
////        net.createFlow(t, sC, new ArcExpression(new Variable("c"))); VERSION A
//        net.createFlow(t, sC, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));// VERSION B
        t = net.createTransition();
        net.createFlow(ship, t, new ArcExpression(new Variable("c")));
//        net.createFlow(t, sC, new ArcExpression(new Variable("c"))); //VERSION A
        net.createFlow(t, sC, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));// VERSION B
        net.createFlow(t, rec, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(rec, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

        // container place
        Place contP = net.createSysPlace("conP", ccp);
        Place bad = net.createSysPlace("bad", c);
        net.setBad(bad);
        t = net.createTransition();
        net.createFlow(contP, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(t, bad, new ArcExpression(new Variable("c")));
        t = net.createTransition();
        net.createFlow(rec, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(contP, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        // next two lines added for Version B
        net.createFlow(e2, t);
        net.createFlow(t, e0);

        // costs
        Place ccost = net.createSysPlace("costs", k);
        net.setColorTokens(ccost, costs);

        // robot
        Place rob = net.createSysPlace("Rob", rb);
        List<ColorToken> init = new ArrayList<>();
        for (int i = 0; i < nb_robots; i++) {
            init.add(new ColorToken(new Color("r" + i), new Color("high")));
        }
        net.setColorTokens(rob, init);
        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.NEQ, new ColorClassTerm("B3")));
//        net.createFlow(sC, t, new ArcExpression(new Variable("c"))); // VERSION A
        net.createFlow(sC, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));// VERSION B
        ArcTuple tup = new ArcTuple();
        tup.add(new Variable("r"));
        tup.add(new SuccessorTerm(new Variable("b"), net));
        net.createFlow(rob, t, new ArcExpression(tup));
        net.createFlow(t, rob, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(t, contP, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.NEQ, new ColorClassTerm("B3")));
        net.createFlow(rob, t, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(ccost, t, new ArcExpression(new Variable("z")));
        net.createFlow(t, rob, new ArcExpression(tup));

        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.EQ, new ColorClassTerm("B3")));
        net.createFlow(rob, t, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(ccost, t, new ArcExpression(new Variable("z")));
        net.createFlow(t, rob, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));

        return net;
    }

    /**
     * First idea which has a problem with the new non determinism and most
     * likely also for more than one robot with real nondet.
     *
     * @param size
     * @param withPartition
     * @return
     */
    public static HLPetriGame generateA(int nb_container, int nb_ct_places, int nb_robots, int nb_costs, boolean withPartition) {
//        if (size < 1) {
//            throw new RuntimeException("There should at least be one Clerk to sign the document.");
//        }
        HLPetriGame net = new HLPetriGame("High-Level ");
//        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        if (withPartition) {
            Map<String, Integer> partitions = new HashMap<>();
            for (int i = 0; i < nb_container; i++) {
                partitions.put("eC_c" + i, nb_container + 1 + i);
                for (int j = 0; j < nb_ct_places; j++) {
                    partitions.put("rec_c" + i + "xcp" + j, nb_container + 1 + i);
                }
                partitions.put("ship_c" + i, i + 1);
                partitions.put("sC_c" + i, i + 1);
                partitions.put("bad_c" + i, i + 1);
                for (int j = 0; j < nb_ct_places; j++) {
                    partitions.put("conP_c" + i + "xcp" + j, i + 1);
                }
            }
            int actToken = nb_container + nb_container + 1;
            for (int i = 0; i < nb_robots; i++) {
                partitions.put("Rob_r" + i + "xdead", actToken + i);
                partitions.put("Rob_r" + i + "xlow", actToken + i);
                partitions.put("Rob_r" + i + "xmedium", actToken + i);
                partitions.put("Rob_r" + i + "xhigh", actToken + i);
            }

            for (int i = 0; i < nb_costs; i++) {
                partitions.put("costs_m" + i, actToken + nb_robots + i);
            }

            net.putExtension("partitions", partitions);
        }
        // create the color classes
        Color[] cont = new Color[nb_container];
        for (int i = 0; i < cont.length; i++) {
            cont[i] = new Color("c" + i);
        }
        Color[] cp = new Color[nb_ct_places];
        for (int i = 0; i < cp.length; i++) {
            cp[i] = new Color("cp" + i);
        }
        Color[] robots = new Color[nb_robots];
        for (int i = 0; i < robots.length; i++) {
            robots[i] = new Color("r" + i);
        }
        Color[] costs = new Color[nb_costs];
        for (int i = 0; i < costs.length; i++) {
            costs[i] = new Color("m" + i);
        }

        List<Pair<String, String[]>> batteries = new ArrayList<>();
        batteries.add(new Pair<>("B0", new String[]{"dead"}));
        batteries.add(new Pair<>("B1", new String[]{"low"}));
        batteries.add(new Pair<>("B2", new String[]{"medium"}));
        batteries.add(new Pair<>("B3", new String[]{"high"}));

        net.createBasicColorClass("E", false, "e");
        net.createBasicColorClass("C", false, cont);
        net.createBasicColorClass("CP", false, cp);
        net.createBasicColorClass("R", false, robots);
        net.createBasicColorClassByStaticSubClass("B", true, batteries);
        net.createBasicColorClass("K", false, costs);

        // possible color domains
        String[] ec = {"E"};
        String[] c = {"C"};
        String[] p = {"CP"};
        String[] ccp = {"C", "CP"};
        String[] rb = {"R", "B"};
        String[] k = {"K"};

        Place e0 = net.createEnvPlace("e0", ec);
        net.setColorTokens(e0, "e");
        Place e1 = net.createEnvPlace("e1", p);
        Transition t = net.createTransition();
        net.createFlow(e0, t);
        net.createFlow(t, e1, new ArcExpression(new Variable("p")));

        // tagging
        Place eC = net.createSysPlace("eC", c);
        net.setColorTokens(eC, cont);
        Place rec = net.createSysPlace("rec", ccp);
        t = net.createTransition();
        net.createFlow(e1, t, new ArcExpression(new Variable("p")));
        net.createFlow(eC, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, rec, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(t, e0);

        // ship
        Place ship = net.createSysPlace("ship", c);
        net.setColorTokens(ship, cont);
        Place sC = net.createSysPlace("sC", c);
        t = net.createTransition();
        net.createFlow(ship, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, sC, new ArcExpression(new Variable("c")));
        t = net.createTransition();
        net.createFlow(ship, t, new ArcExpression(new Variable("c")));
        net.createFlow(t, sC, new ArcExpression(new Variable("c")));
        net.createFlow(t, rec, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(rec, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

        // container place
        Place contP = net.createSysPlace("conP", ccp);
        Place bad = net.createSysPlace("bad", c);
        net.setBad(bad);
        t = net.createTransition();
        net.createFlow(contP, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(t, bad, new ArcExpression(new Variable("c")));
        t = net.createTransition();
        net.createFlow(rec, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));
        net.createFlow(contP, t, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

        // costs
        Place ccost = net.createSysPlace("costs", k);
        net.setColorTokens(ccost, costs);

        // robot
        Place rob = net.createSysPlace("Rob", rb);
        List<ColorToken> init = new ArrayList<>();
        for (int i = 0; i < nb_robots; i++) {
            init.add(new ColorToken(new Color("r" + i), new Color("high")));
        }
        net.setColorTokens(rob, init);
        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.NEQ, new ColorClassTerm("B3")));
        net.createFlow(sC, t, new ArcExpression(new Variable("c")));
        ArcTuple tup = new ArcTuple();
        tup.add(new Variable("r"));
        tup.add(new SuccessorTerm(new Variable("b"), net));
        net.createFlow(rob, t, new ArcExpression(tup));
        net.createFlow(t, rob, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(t, contP, new ArcExpression(new ArcTuple(new Variable("c"), new Variable("p"))));

        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.NEQ, new ColorClassTerm("B3")));
        net.createFlow(rob, t, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(ccost, t, new ArcExpression(new Variable("z")));
        net.createFlow(t, rob, new ArcExpression(tup));

        t = net.createTransition(new BasicPredicate(new DomainTerm(new Variable("b"), net), BasicPredicate.Operator.EQ, new ColorClassTerm("B3")));
        net.createFlow(rob, t, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));
        net.createFlow(ccost, t, new ArcExpression(new Variable("z")));
        net.createFlow(t, rob, new ArcExpression(new ArcTuple(new Variable("r"), new Variable("b"))));

        return net;
    }

}
