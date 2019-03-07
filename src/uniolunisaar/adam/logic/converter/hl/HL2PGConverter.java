package uniolunisaar.adam.logic.converter.hl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColorDomain;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.petrigame.PetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public class HL2PGConverter {

    private static final String ID_DELIM = "-";
    private static final String COLOR_DELIM = "x";

    public static PetriGame convert(HLPetriGame hlgame) {
        PetriGame pg = new PetriGame(hlgame.getName() + " - LL-Version");
        // Places
        addPlaces(hlgame, pg);
        // set initial marking
        setInitialMarking(hlgame, pg);
        // transitions
        addTransitions(hlgame, pg);
        return pg;
    }

    private static void setInitialMarking(HLPetriGame hlgame, PetriGame pg) {
        for (Place place : hlgame.getPlaces()) {
            if (hlgame.hasColorToken(place)) {
                ColorToken token = hlgame.getColorToken(place);
                for (Color color : token) {
                    Place p = pg.getPlace(place.getId() + ID_DELIM + color.getId()); // TODO: do it here correctly for ColorToken which are not a basic color
                    p.setInitialToken(1);
                }
            }
        }
    }

    private static void addPlaces(HLPetriGame hlgame, PetriGame pg) {
        for (Place place : hlgame.getPlaces()) {
            ColorDomain dom = hlgame.getColorDomain(place);
            boolean env = hlgame.isEnvironment(place);
            boolean special = hlgame.isSpecial(place);
            if (dom.size() <= 1) {
                BasicColorClass bcc = hlgame.getBasicColorClass(dom.get(0));
                for (Color color : bcc.getColors()) {
                    Place p = pg.createPlace(place.getId() + ID_DELIM + color.getId());
                    if (env) {
                        pg.setEnvironment(p);
                    } else {
                        pg.setSystem(p);
                    }
                    if (special) {
                        pg.setBad(p);
                    }
                }
            } else if (dom.size() == 2) {
//                createPlace(dom, 0, place.getId(), env, hlgame, pg);
                BasicColorClass bcc1 = hlgame.getBasicColorClass(dom.get(0));
                BasicColorClass bcc2 = hlgame.getBasicColorClass(dom.get(1));
                for (Color c1 : bcc1.getColors()) {
                    for (Color c2 : bcc2.getColors()) {
                        Place p = pg.createPlace(place.getId() + ID_DELIM + c1.getId() + COLOR_DELIM + c2.getId());
                        if (env) {
                            pg.setEnvironment(p);
                        } else {
                            pg.setSystem(p);
                        }
                        if (special) {
                            pg.setBad(p);
                        }
                    }
                }
            } else {
                throw new UnsupportedOperationException("Color domains with more than two basic color classes are not yet supported.");
            }
        }
    }

    private static void addTransitions(HLPetriGame hlgame, PetriGame pg) {
        for (Transition t : hlgame.getTransitions()) {
            IPredicate pred = hlgame.getPredicate(t);
            Set<Variable> vars = hlgame.getVariables(t);
            // Get variable to color domain
            Map<Variable, List<Color>> var2CClass = new HashMap<>();
            for (Flow presetEdge : t.getPresetEdges()) {
                Place pre = presetEdge.getPlace();
                BasicColorClass[] bcs = hlgame.getBasicColorClasses(pre);
            }
            Valuation val = new Valuation();

        }
    }

    /**
     * TODO: Finish this method which creates the places of the cartesian
     * product for an arbitrary number of basic color classes.
     *
     * @param dom
     * @param idx
     * @param placeName
     * @param env
     * @param hlgame
     * @param pg
     */
    private static void createPlace(ColorDomain dom, int idx, String placeName, boolean env, HLPetriGame hlgame, PetriGame pg) {
        if (idx == dom.size()) {
            Place p = pg.createPlace(placeName);
            if (env) {
                pg.setEnvironment(p);
            } else {
                pg.setSystem(p);
            }
        } else {
            BasicColorClass bcc = hlgame.getBasicColorClass(dom.get(0));
            for (Color color : bcc.getColors()) {

            }
        }
    }
}
