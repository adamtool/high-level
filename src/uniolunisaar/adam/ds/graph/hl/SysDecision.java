package uniolunisaar.adam.ds.graph.hl;

import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.highlevel.ColorToken;

/**
 *
 * @author Manuel Gieseking
 */
public class SysDecision implements IDecision {

    private final Place place;
    private final ColorToken color;
    private final boolean type;
    private final CommitmentSet c;

    public SysDecision(Place place, ColorToken color, boolean type, CommitmentSet c) {
        this.place = place;
        this.color = color;
        this.type = type;
        this.c = c;
    }

    @Override
    public boolean isEnvDecision() {
        return false;
    }

    public Place getPlace() {
        return place;
    }

    public ColorToken getColor() {
        return color;
    }

    public boolean isType() {
        return type;
    }

    public CommitmentSet getC() {
        return c;
    }

}
