package uniolunisaar.adam.ds.graph.hl.approachHL;

import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.graph.hl.EnvDecision;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 */
public class HLEnvDecision extends EnvDecision<ColoredPlace, ColoredTransition> implements IHLDecision {

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
}
