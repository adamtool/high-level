package uniolunisaar.adam.generators.hl;

import uniolunisaar.adam.generators.pg.*;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.logic.pg.calculators.CalculatorIDs;
import uniolunisaar.adam.logic.pg.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.util.PGTools;
import uniolunisaar.adam.util.PNWTTools;

/**
 *
 * @author Manuel Gieseking
 */
public class ConcurrentMachinesHL {

    /**
     * Version for Bengt Jonsson Festschrift
     *
     * @param machines
     * @param orders
     * @return
     */
    public static PetriGame generateImprovedVersion(int machines, int orders) {
        if (machines < 2 || orders < 1) {
            throw new RuntimeException("less than 2 machines or 1 order does not make any sense!");
        }
        PetriGame net = PGTools.createPetriGame("CM_" + "M" + machines + "WP" + orders);
        MaxTokenCountCalculator calc = new MaxTokenCountCalculator();
        net.addExtensionCalculator(CalculatorIDs.MAX_TOKEN_COUNT.name(), calc, true);
        PNWTTools.setConditionAnnotation(net, Condition.Objective.A_SAFETY);

        // Environment
        Place start = net.createEnvPlace("Env");
        start.setInitialToken(1);
       
//        Place stop = net.createEnvPlace("e"); should not be necessary anymore to always have an env place

        Place[] macs = new Place[machines];
        // testing
        Place test = net.createPlace("testP");
        net.setPartition(test, orders + 1);
        // activing all, but one
        Transition[] trans = new Transition[machines];
        for (int i = 0; i < machines; ++i) {
            macs[i] = net.createPlace("A" + i);
            trans[i] = net.createTransition();
            //environment
            net.createFlow(start, trans[i]);
//            net.createFlow(trans[i], stop);should not be necessary anymore to always have an env place
        }
        for (int i = 0; i < machines; ++i) {
            for (int j = 0; j < machines; ++j) {
                if (i != j) {
                    net.createFlow(trans[i], macs[j]);
                }
            }
            net.createFlow(trans[i], test);
        }

        // test transition
        Transition testT = net.createTransition("test");
        net.createFlow(test, testT);
        for (int i = 0; i < orders; ++i) {
            Place s = net.createPlace("S" + i);
            s.setInitialToken(1);
            //testing: version all orders together
            net.createFlow(testT, s);
            net.createFlow(s, testT);

            // testing: version when each order does it separatly (still must adds things that not the type2 strategy (infinitely testing) is winning)
//            Transition testT = net.createTransition("test" + i);
//            net.createFlow(test, testT);
//            net.createFlow(testT, test);
//            net.createFlow(testT, s);
//            net.createFlow(s, testT);
            for (int j = 0; j < machines; ++j) {
                // working
                Place m = net.createPlace("M" + j + "" + i);
                Transition t = net.createTransition();
                net.createFlow(s, t);
                net.createFlow(t, m);
                Place bad = net.createPlace("B" + j + "" + i);
                net.setBad(bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(t, bad);
                t = net.createTransition();
                net.createFlow(m, t);
                net.createFlow(macs[j], t);
                Place mready = net.createPlace("G" + j + "" + i);
                net.createFlow(t, mready);                
            }
        }

        return net;
    }
}
