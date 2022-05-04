package uniolunisaar.adam.generators.highlevel;

import java.util.HashMap;
import java.util.Map;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;

/**
 * Creates a whac-a-mole example. The sensors of a robotic arm detect the mole
 * and the arm has to hit the correct location.
 *
 * @author Manuel Gieseking
 */
public class WhacAMole {

    public static HLPetriGame create(int nb_locations, boolean withPartition) {
        if (nb_locations < 1) {
            throw new RuntimeException("Less than one location is not a game!");
        }
        HLPetriGame net = new HLPetriGame("High-Level Whac-A-Mole game with " + nb_locations + " locations.");

        if (withPartition) {
            Map<String, Integer> partitions = new HashMap<>();
            for (int i = 0; i < nb_locations; i++) {
                partitions.put("A_H_l" + i, 1);
            }
            partitions.put("A_dot", 1);
            partitions.put("A_B_dot", 1);
            net.putExtension("partitions", partitions);
        }

        // create the color classes
        Color[] colors = new Color[nb_locations];
        for (int i = 0; i < nb_locations; i++) {
            colors[i] = new Color("l" + i);
        }
        net.createBasicColorClass("locations", false, colors);
        net.createBasicColorClass("dot", false, "dot");
        // color classes        
        String[] dot = {"dot"};
        String[] locations = {"locations"};

        // Select location
        Place env0 = net.createEnvPlace("E_0", dot);
        net.setColorTokens(env0, "dot");
        Place env1 = net.createEnvPlace("E_1", locations);
        Place infArm = net.createSysPlace("A", dot);
        Transition t = net.createTransition("sens_loc");
        net.createFlow(env0, t);
        net.createFlow(t, env1, new ArcExpression(new Variable("l")));
        net.createFlow(t, infArm);
        // hit locations
        Place hitMole = net.createSysPlace("A_H", locations);
        Transition tHit = net.createTransition("hit_loc");
        net.createFlow(infArm, tHit);
        net.createFlow(tHit, hitMole, new ArcExpression(new Variable("l")));
        // matched
        Transition tMatch = net.createTransition("matched_loc");
        net.createFlow(hitMole, tMatch, new ArcExpression(new Variable("l")));
        net.createFlow(env1, tMatch, new ArcExpression(new Variable("l")));
        net.createFlow(tMatch, env0);
        // missed
        Place missed = net.createSysPlace("A_B", dot);
        net.setBad(missed);
        Transition tMiss = net.createTransition("missed_loc");
        net.createFlow(hitMole, tMiss, new ArcExpression(new Variable("l")));
        net.createFlow(tMiss, missed);
        return net;
    }

}
