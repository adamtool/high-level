package uniolunisaar.adam.ds.highlevel;

import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.util.AdamExtensions;

/**
 *
 * @author Manuel Gieseking
 */
public class HLPetriGameExtensionHandler {

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PLACE EXTENSIONS   
    static ColorDomain getColorDomain(Place place) {
        return (ColorDomain) place.getExtension(AdamExtensions.colorDomain.name());
    }

    static void setColorClasses(Place place, ColorDomain domain) {
        place.putExtension(AdamExtensions.colorDomain.name(), domain, ExtensionProperty.WRITE_TO_FILE);
    }

    static boolean hasColorToken(Place place) {
        return place.hasExtension(AdamExtensions.colorToken.name());
    }

    static ColorToken getColorToken(Place place) {
        return (ColorToken) place.getExtension(AdamExtensions.colorToken.name());
    }

    static void setColorToken(Place place, ColorToken token) {
        place.putExtension(AdamExtensions.colorToken.name(), token, ExtensionProperty.WRITE_TO_FILE);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TRANSITION EXTENSIONS   
    static IPredicate getPredicate(Transition transition) {
        return (IPredicate) transition.getExtension(AdamExtensions.predicate.name());
    }

    static void setPredicate(Transition transition, IPredicate pred) {
        transition.putExtension(AdamExtensions.predicate.name(), pred, ExtensionProperty.WRITE_TO_FILE);
    }

}
