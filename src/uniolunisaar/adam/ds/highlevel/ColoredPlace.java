package uniolunisaar.adam.ds.highlevel;

import java.util.Objects;
import uniol.apt.adt.pn.Place;

/**
 *
 * @author Manuel Gieseking
 */
public class ColoredPlace {

    private final Place place;
    private final ColorToken color;

    public ColoredPlace(ColoredPlace place) {
        this.place = place.getPlace();
        this.color = new ColorToken(place.getColor());
    }

    public ColoredPlace(Place place, ColorToken color) {
        this.place = place;
        this.color = color;
    }

    public Place getPlace() {
        return place;
    }

    public ColorToken getColor() {
        return color;
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
        final ColoredPlace other = (ColoredPlace) obj;
        if (!Objects.equals(this.place, other.place)) {
            return false;
        }
        if (!Objects.equals(this.color, other.color)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.place);
        hash = 37 * hash * Objects.hashCode(this.color);
        return hash;
    }

    @Override
    public String toString() {
        return place.getId() + "." + color.toString();
    }
}
