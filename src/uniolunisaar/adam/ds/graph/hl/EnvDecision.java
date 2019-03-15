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
public class EnvDecision implements IDecision {

    private final ColoredPlace place;

    public EnvDecision(EnvDecision dc) {
        place = new ColoredPlace(dc.getPlace());
    }

    public EnvDecision(ColoredPlace place) {
        this.place = place;
    }

    public EnvDecision(Place place, ColorToken color) {
        this(new ColoredPlace(place, color));
    }

    @Override
    public void apply(Symmetry sym) {
        place.getColor().apply(sym);
    }

    @Override
    public boolean isEnvDecision() {
        return true;
    }

    @Override
    public ColoredPlace getPlace() {
        return place;
    }

    @Override
    public boolean isChoosen(ColoredTransition t) {
        return true;
    }

    @Override
    public boolean isTop() {
        return false;
    }

    @Override
    public String toDot() {
        return place.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + Objects.hashCode(this.place);
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
        final EnvDecision other = (EnvDecision) obj;
        if (!Objects.equals(this.place, other.place)) {
            return false;
        }
        return true;
    }

}
