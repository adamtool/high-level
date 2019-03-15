package uniolunisaar.adam.ds.highlevel;

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

}
