package uniolunisaar.adam.ds.graph.hl.approachHL;

import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.graph.hl.SysDecision;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 */
public class HLSysDecision extends SysDecision<ColoredPlace, ColoredTransition, HLCommitmentSet> implements IHLDecision {

    public HLSysDecision(HLSysDecision dcs) {
        super(new ColoredPlace(dcs.place), new HLCommitmentSet(dcs.c));
    }

    public HLSysDecision(Place place, ColorToken color, HLCommitmentSet c) {
        super(new ColoredPlace(place, color), c);
    }

    public HLSysDecision(ColoredPlace place, HLCommitmentSet c) {
        super(place, c);
    }

    @Override
    public void apply(Symmetry sym) {
        place.getColor().apply(sym);
        c.apply(sym);
    }

}
