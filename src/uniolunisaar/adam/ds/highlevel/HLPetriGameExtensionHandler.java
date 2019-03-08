package uniolunisaar.adam.ds.highlevel;

import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
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

    static boolean hasColorTokens(Place place) {
        return place.hasExtension(AdamExtensions.colorTokens.name()) && !((ColorTokens) place.getExtension(AdamExtensions.colorTokens.name())).isEmpty();
    }

    static ColorTokens getColorTokens(Place place) {
        return (ColorTokens) place.getExtension(AdamExtensions.colorTokens.name());
    }

    static void setColorTokens(Place place, ColorTokens token) {
        place.putExtension(AdamExtensions.colorTokens.name(), token, ExtensionProperty.WRITE_TO_FILE);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TRANSITION EXTENSIONS   
    static IPredicate getPredicate(Transition transition) {
        return (IPredicate) transition.getExtension(AdamExtensions.predicate.name());
    }

    static void setPredicate(Transition transition, IPredicate pred) {
        transition.putExtension(AdamExtensions.predicate.name(), pred, ExtensionProperty.WRITE_TO_FILE);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% FLOW EXTENSIONS   
    static boolean hasArcExpression(Flow flow) {
        return flow.hasExtension(AdamExtensions.arcExpression.name());
    }

    static ArcExpression getArcExpression(Flow flow) {
        return (ArcExpression) flow.getExtension(AdamExtensions.arcExpression.name());
    }

    static void setArcExpression(Flow flow, ArcExpression expr) {
        flow.putExtension(AdamExtensions.arcExpression.name(), expr, ExtensionProperty.WRITE_TO_FILE);
    }

}
