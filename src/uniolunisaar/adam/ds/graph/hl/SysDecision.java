package uniolunisaar.adam.ds.graph.hl;

import java.util.Objects;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

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
    
    public SysDecision(SysDecision dcs) {
        place = new ColoredPlace(dcs.place);
        c = new CommitmentSet(dcs.c);
    }
    
    public SysDecision(ColoredPlace place, CommitmentSet c) {
        this.place = place;
        this.c = c;
    }

    public SysDecision(Place place, ColorToken color, CommitmentSet c) {
        this(new ColoredPlace(place, color), c);
    }

    @Override
    public void apply(Symmetry sym) {
        place.getColor().apply(sym);
        c.apply(sym);
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.place);
        hash = 29 * hash + Objects.hashCode(this.c);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SysDecision other = (SysDecision) obj;
        if (!Objects.equals(this.place, other.place)) {
            return false;
        }
        if (!Objects.equals(this.c, other.c)) {
            return false;
        }
        return true;
    }

}
