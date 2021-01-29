package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach;

import java.util.ArrayList;
import java.util.List;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.EnvDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class LLEnvDecision extends EnvDecision {

    public LLEnvDecision(PetriGameWithTransits game, Place place) {
        super(game, place);
    }

    public LLEnvDecision(LLEnvDecision dc) {
        super(dc);
    }

    public LLEnvDecision(EnvDecision dc) {
        super(dc);
    }

//    @Override
//    public void apply(Symmetry sym) {
////        String id = HL2PGConverter.getHLPlaceID(place.getId());
////        String[] col = HL2PGConverter.getPlaceColorIDs(place.getId());
////        List<Color> colors = new ArrayList<>();
////        for (int i = 0; i < col.length - 1; i++) {
////            colors.add(sym.get(new Color(col[i])));
////        }
////        place = game.getPlace(HL2PGConverter.getPlaceID(id, colors));
//        String id = HL2PGConverter.getOrigID(place);
//        List<Color> col = HL2PGConverter.getColors(place);
//        List<Color> colors = new ArrayList<>();
//        for (int i = 0; i < col.size(); i++) {
//            colors.add(sym.get(col.get(i)));
//        }
//        place = game.getPlace(HL2PGConverter.getPlaceID(id, colors));
//    }
    @Override
    public LLEnvDecision apply(Symmetry sym) {
        String id = HL2PGConverter.getOrigID(getPlace());
        List<Color> col = HL2PGConverter.getColors(getPlace());
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < col.size(); i++) {
            colors.add(sym.get(col.get(i)));
        }
        return new LLEnvDecision(getGame(), getGame().getPlace(HL2PGConverter.getPlaceID(id, colors)));
    }

}
