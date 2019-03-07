package uniolunisaar.adam.logic.converter.hl;

import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColorDomain;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.petrigame.PetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public class HL2PGConverter {

    public static PetriGame convert(HLPetriGame hlgame) {
        PetriGame pg = new PetriGame(hlgame.getName() + " - LL-Version");
        // Places
        for (Place place : hlgame.getPlaces()) {
            ColorDomain dom = hlgame.getColorDomain(place);
            boolean env = hlgame.isEnvironment(place);
            boolean special = hlgame.isSpecial(place);
            if (dom.size() <= 1) {
                BasicColorClass bcc = hlgame.getBasicColorClass(dom.get(0));
                for (Color color : bcc.getColors()) {
                    Place p = pg.createPlace(place.getId() + "-" + color.getId());
                    if (env) {
                        pg.setEnvironment(p);
                    } else {
                        pg.setSystem(p);
                    }
                    if (special) {
                        pg.setBad(p);
                    }
                }
            } else {
                createPlace(dom, 0, place.getId(), env, hlgame, pg);
            }
        }
        return pg;
    }

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
