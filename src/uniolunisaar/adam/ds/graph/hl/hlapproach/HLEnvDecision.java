package uniolunisaar.adam.ds.graph.hl.hlapproach;

import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.graph.AbstractEnvDecision;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 */
public class HLEnvDecision extends AbstractEnvDecision<ColoredPlace, ColoredTransition> implements IHLDecision {

    public HLEnvDecision(ColoredPlace place) {
        super(place);
    }

    public HLEnvDecision(HLEnvDecision dc) {
        super(new ColoredPlace(dc.getPlace()));
    }

    public HLEnvDecision(Place place, ColorToken color) {
        this(new ColoredPlace(place, color));
    }

//    @Override
//    public void apply(Symmetry sym) {
//        getPlace().getColor().apply(sym);
//    }
    @Override
    public HLEnvDecision apply(Symmetry sym) {
        return new HLEnvDecision(getPlace().apply(sym));
    }

    /**
     * Attention: it is not checked if t is valid for 
     * cost saving reasons.
     * @param t
     * @return 
     */
    @Override
    public boolean isChoosen(ColoredTransition t) {
//        return getPlace().getPlace().getPostset().contains(t.getTransition());
// this above is less expensive and currently seems to deliver the same results,
// but not sure if this would work in any case
        return t.getPreset().contains(getPlace());
    }
}
