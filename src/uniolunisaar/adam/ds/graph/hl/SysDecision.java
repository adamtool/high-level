package uniolunisaar.adam.ds.graph.hl;

import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;

/**
 *
 * @author Manuel Gieseking
 */
public class SysDecision implements IDecision {

    private final ColoredPlace place;
//    private final boolean type;
    private final CommitmentSet c;

//    public SysDecision(Place place, ColorToken color, boolean type, CommitmentSet c) {
//        this.place = place;
//        this.color = color;
//        this.type = type;
//        this.c = c;
//    }
    public SysDecision(ColoredPlace place, CommitmentSet c) {
        this.place = place;
        this.c = c;
    }

    public SysDecision(Place place, ColorToken color, CommitmentSet c) {
        this(new ColoredPlace(place, color), c);
    }

    @Override
    public boolean isEnvDecision() {
        return false;
    }

    @Override
    public ColoredPlace getPlace() {
        return place;
    }

    @Override
    public boolean isChoosen(ColoredTransition t) {
        return c.isChoosen(t);
    }

    @Override
    public boolean isTop() {
        return c.isTop;
    }

//    public boolean isType() {
//        return type;
//    }
    public CommitmentSet getC() {
        return c;
    }

}
