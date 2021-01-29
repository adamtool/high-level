package uniolunisaar.adam.generators.highlevel;

import java.util.HashMap;
import java.util.Map;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;

/**
 * @author Manuel Gieseking
 */
public class ClientServerHL {

    /**
     *
     * Creates the example for the client server system of the canon21 paper.
     *
     * @param nb_clients
     * @param withPartition
     * @return
     */
    public static HLPetriGame create(int nb_clients, boolean withPartition) {
        if (nb_clients < 1) {
            throw new RuntimeException("At least one client ist needed.");
        }
        HLPetriGame net = new HLPetriGame("High-Level Client Server example with " + nb_clients + " clients.");

        if (withPartition) {
            Map<String, Integer> partitions = new HashMap<>();
            for (int i = 0; i < nb_clients; i++) {
                partitions.put("Sys_c" + i, (i + 1));
                partitions.put("H_c" + i, (i + 1));
                partitions.put("B_c" + i, (i + 1));
                for (int j = 0; j < nb_clients; j++) {
                    partitions.put("A_c" + i + "xc" + j, (i + 1));
                }
            }
            net.putExtension("partitions", partitions);
        }

        // create the color classes
        Color[] colors = new Color[nb_clients];
        for (int i = 0; i < nb_clients; i++) {
            colors[i] = new Color("c" + i);
        }
        net.createBasicColorClass("clients", false, colors);
        net.createBasicColorClass("server", false, "s");
        // color classes        
        String[] clientsColorClass = {"clients"};
        String[] serverColorClass = {"server"};
        String[] clientsTupleColorClass = {"clients", "clients"};

        // Environment
        Place env = net.createEnvPlace("env", serverColorClass);
        net.setColorTokens(env, "s");
        Place env1 = net.createEnvPlace("I", clientsColorClass);
        Transition t = net.createTransition("d");
        net.createFlow(env, t, new ArcExpression(new Variable("dot")));
        net.createFlow(t, env1, new ArcExpression(new Variable("x")));
        Place env3 = net.createEnvPlace("R", clientsColorClass);
        // system
        Place sys = net.createSysPlace("Sys", clientsColorClass);
        net.setColorTokens(sys, colors);
        Place a = net.createSysPlace("A", clientsTupleColorClass);
        Place b = net.createSysPlace("B", clientsColorClass);
        net.setBad(b);
        Place h = net.createSysPlace("H", clientsColorClass);

        t = net.createTransition("inf");
        net.createFlow(env1, t, new ArcExpression(new Variable("x")));
        net.createFlow(sys, t, new ArcExpression(new ColorClassTerm("clients")));
        net.createFlow(t, env3, new ArcExpression(new Variable("x")));
        net.createFlow(t, sys, new ArcExpression(new ColorClassTerm("clients")));

        t = net.createTransition("a");
        net.createFlow(sys, t, new ArcExpression(new Variable("y")));
        net.createFlow(t, a, new ArcExpression(new ArcTuple(new Variable("y"), new Variable("x"))));

        t = net.createTransition("b");
        net.createFlow(a, t, new ArcExpression(new ArcTuple(new Variable("y"), new Variable("x"))));
        net.createFlow(t, b, new ArcExpression(new Variable("y")));

        t = net.createTransition("h");
        net.createFlow(env3, t, new ArcExpression(new Variable("x")));
        ArcTuple arcTuple = new ArcTuple();
        arcTuple.add(new ColorClassTerm("clients"));
        arcTuple.add(new Variable("x"));
        net.createFlow(a, t, new ArcExpression(arcTuple));
//        net.createFlow(a, t, new ArcExpression(new SetMinusTerm(new ColorClassTerm("clients"), new Variable("x"))));
//        List<IPredicate> preds = new ArrayList<>();
//        for (int i = 0; i < nb_clients; i++) {
//            net.createFlow(a, t, new ArcExpression(new ArcTuple(new Variable("col" + i), new Variable("x"))));
//            preds.add(new BasicPredicate(new DomainTerm(new Variable("col" + i), net), BasicPredicate.Operator.EQ, new ColorClassTerm("csub0")));// need subclasses for this
//        }
        net.createFlow(t, h, new ArcExpression(new Variable("x")));

        return net;
    }

}
