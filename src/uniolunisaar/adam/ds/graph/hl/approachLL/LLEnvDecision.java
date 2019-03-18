package uniolunisaar.adam.ds.graph.hl.approachLL;

import java.util.ArrayList;
import java.util.List;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.EnvDecision;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class LLEnvDecision extends EnvDecision<Place, Transition> implements ILLDecision {

    private final PetriGame game;

    public LLEnvDecision(PetriGame game, Place place) {
        super(place);
        this.game = game;
    }

    public LLEnvDecision(PetriGame game, LLEnvDecision dc) {
        // here is a copy of the references OK
        // (as long as no one uses the extensions such that it is not OK)
        super(dc.getPlace());
        this.game = game;
    }

    @Override
    public void apply(Symmetry sym) {
//        String id = HL2PGConverter.getHLPlaceID(place.getId());
//        String[] col = HL2PGConverter.getPlaceColorIDs(place.getId());
//        List<Color> colors = new ArrayList<>();
//        for (int i = 0; i < col.length - 1; i++) {
//            colors.add(sym.get(new Color(col[i])));
//        }
//        place = game.getPlace(HL2PGConverter.getPlaceID(id, colors));
        String id = HL2PGConverter.getOrigID(place);
        List<Color> col = HL2PGConverter.getColors(place);
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < col.size(); i++) {
            colors.add(sym.get(col.get(i)));
        }
        place = game.getPlace(HL2PGConverter.getPlaceID(id, colors));
    }

}
