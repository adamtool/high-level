package uniolunisaar.adam.ds.graph.hl.hlapproach;

import java.util.concurrent.ForkJoinPool;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.graph.AbstractSysDecision;
import uniolunisaar.adam.ds.graph.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.hl.llapproach.LLSysDecision;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class HLSysDecision extends AbstractSysDecision<ColoredPlace, ColoredTransition, HLCommitmentSet> implements IHLDecision {

    /**
     * Copy-Constructor
     *
     * @param dcs
     */
    public HLSysDecision(HLSysDecision dcs) {
        super(new ColoredPlace(dcs.getPlace()), new HLCommitmentSet(dcs.getC()));
    }

    public HLSysDecision(Place place, ColorToken color, HLCommitmentSet c) {
        super(new ColoredPlace(place, color), c);
    }

    public HLSysDecision(ColoredPlace place, HLCommitmentSet c) {
        super(place, c);
    }

//    @Override
//    public void apply(Symmetry sym) {
//        place.getColor().apply(sym);
//        c.apply(sym);
//    }
    @Override
    public HLSysDecision apply(Symmetry sym) {
        ColoredPlace place = getPlace().apply(sym);
        HLCommitmentSet c = getC().apply(sym);
        return new HLSysDecision(place, c);
    }

    @Override
    @Deprecated
    public ILLDecision toLLDecision(PetriGameWithTransits game) {
        Place p =  game.getPlace(HL2PGConverter.getPlaceID(getPlace().getPlace().getId(), getPlace().getColor()));
        return new LLSysDecision(game, p, getC().toLLCommitmentSet(game));
    }

}
