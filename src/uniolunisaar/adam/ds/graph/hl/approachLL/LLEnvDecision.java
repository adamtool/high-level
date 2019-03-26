package uniolunisaar.adam.ds.graph.hl.approachLL;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

    public LLEnvDecision(LLEnvDecision dc) {
        // here is a copy of the references OK
        // (as long as no one uses the extensions such that it is not OK)
        super(dc.getPlace());
        this.game = dc.game;
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
        return new LLEnvDecision(game, game.getPlace(HL2PGConverter.getPlaceID(id, colors)));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + HL2PGConverter.getHashCode(getPlace());
        return hash;
    }
    
    @Override
    public boolean isChoosen(Transition t) {
        return getPlace().getPostset().contains(t);
    }

    @Override
    public String toDot() {
        return getPlace().getId();
    }

    @Override
    public String toString() {
        return toDot();
    }

}
