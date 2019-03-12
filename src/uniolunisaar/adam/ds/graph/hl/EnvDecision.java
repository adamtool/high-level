package uniolunisaar.adam.ds.graph.hl;

import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.highlevel.ColorToken;

/**
 *
 * @author Manuel Gieseking
 */
public class EnvDecision implements IDecision {

    private final Place place;
    private final ColorToken color;

    public EnvDecision(Place place, ColorToken color) {
        this.place = place;
        this.color = color;
    }

    @Override
    public boolean isEnvDecision() {
        return true;
    }

    public Place getPlace() {
        return place;
    }

    public ColorToken getColor() {
        return color;
    }

}
