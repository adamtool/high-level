package uniolunisaar.adam.ds.highlevel;

import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.Place;
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

}
