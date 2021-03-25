package uniolunisaar.adam.ds.synthesis.highlevel;

import uniol.apt.adt.extension.ExtensionProperty;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.synthesis.highlevel.predicate.IPredicate;
import uniolunisaar.adam.util.AdamHLExtensions;
import uniolunisaar.adam.util.ExtensionManagement;

/**
 *
 * @author Manuel Gieseking
 */
public class HLPetriGameExtensionHandler {

    // register the Extensions for the framework
    static {
        ExtensionManagement.getInstance().registerExtensions(true, AdamHLExtensions.values());
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% PLACE EXTENSIONS   
    static ColorDomain getColorDomain(Place place) {
        return ExtensionManagement.getInstance().getExtension(place, AdamHLExtensions.colorDomain, ColorDomain.class);
    }

    static void setColorClasses(Place place, ColorDomain domain) {
        ExtensionManagement.getInstance().putExtension(place, AdamHLExtensions.colorDomain, domain, ExtensionProperty.WRITE_TO_FILE);
    }

    static boolean hasColorTokens(Place place) {
        return ExtensionManagement.getInstance().hasExtension(place, AdamHLExtensions.colorTokens)
                && !(ExtensionManagement.getInstance().getExtension(place, AdamHLExtensions.colorTokens, ColorTokens.class)).isEmpty();
    }

    static ColorTokens getColorTokens(Place place) {
        return ExtensionManagement.getInstance().getExtension(place, AdamHLExtensions.colorTokens, ColorTokens.class);
    }

    static void setColorTokens(Place place, ColorTokens token) {
        ExtensionManagement.getInstance().putExtension(place, AdamHLExtensions.colorTokens, token, ExtensionProperty.WRITE_TO_FILE);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% TRANSITION EXTENSIONS   
    static IPredicate getPredicate(Transition transition) {
        return ExtensionManagement.getInstance().getExtension(transition, AdamHLExtensions.predicate, IPredicate.class);
    }

    static void setPredicate(Transition transition, IPredicate pred) {
        ExtensionManagement.getInstance().putExtension(transition, AdamHLExtensions.predicate, pred, ExtensionProperty.WRITE_TO_FILE);
    }

// %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% FLOW EXTENSIONS   
    static boolean hasArcExpression(Flow flow) {
        return ExtensionManagement.getInstance().hasExtension(flow, AdamHLExtensions.arcExpression);
    }

    static ArcExpression getArcExpression(Flow flow) {
        return ExtensionManagement.getInstance().getExtension(flow, AdamHLExtensions.arcExpression, ArcExpression.class);
    }

    static void setArcExpression(Flow flow, ArcExpression expr) {
        ExtensionManagement.getInstance().putExtension(flow, AdamHLExtensions.arcExpression, expr, ExtensionProperty.WRITE_TO_FILE);
    }

}
