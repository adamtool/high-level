package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.hlapproach;

import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractEnvDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLEnvDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.ColorToken;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.synthesis.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

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
     * Attention: it is not checked if t is valid for cost saving reasons.
     *
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

    /**
     *
     * This method is only for the creation of the explicit graph strategy.
     *
     * @param game
     * @return
     */
    @Override
    @Deprecated
    public ILLDecision toLLDecision(PetriGameWithTransits game) {
        return new LLEnvDecision(game, game.getPlace(HL2PGConverter.getPlaceID(getPlace().getPlace().getId(), getPlace().getColor())));
    }
}
