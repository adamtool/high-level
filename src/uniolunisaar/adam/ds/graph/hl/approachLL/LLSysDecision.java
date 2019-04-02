package uniolunisaar.adam.ds.graph.hl.approachLL;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.hl.SysDecision;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.logic.converter.hl.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class LLSysDecision extends SysDecision<Place, Transition, LLCommitmentSet> implements ILLDecision {

    private final PetriGame game;

    /**
     * Copy-Constructor
     *
     * @param dcs
     */
    public LLSysDecision(LLSysDecision dcs) {
        super(dcs.getPlace(), new LLCommitmentSet(dcs.getC()));
        this.game = dcs.game;
    }

    public LLSysDecision(PetriGame game, Place place, LLCommitmentSet c) {
        super(place, c);
        this.game = game;
    }

//    @Override
//    public void apply(Symmetry sym) {
//        // apply it to the place
//// old version        
////        String id = HL2PGConverter.getHLPlaceID(place.getId());
////        String[] col = HL2PGConverter.getPlaceColorIDs(place.getId());
////        List<Color> colors = new ArrayList<>();
////        for (int i = 0; i < col.length - 1; i++) {
////            colors.add(sym.get(new Color(col[i])));
////        }
//        String id = HL2PGConverter.getOrigID(place);
//        List<Color> col = HL2PGConverter.getColors(place);
//        List<Color> colors = new ArrayList<>();
//        for (int i = 0; i < col.size(); i++) {
//            colors.add(sym.get(col.get(i)));
//        }
//        place = game.getPlace(HL2PGConverter.getPlaceID(id, colors));
//        // apply it to the commitment set
//        c.apply(sym);
//    }
    @Override
    public LLSysDecision apply(Symmetry sym) {
        // apply it to the place
        String id = HL2PGConverter.getOrigID(getPlace());
        List<Color> col = HL2PGConverter.getColors(getPlace());
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < col.size(); i++) {
            colors.add(sym.get(col.get(i)));
        }
        Place place = game.getPlace(HL2PGConverter.getPlaceID(id, colors));
        // apply it to the commitment set
        LLCommitmentSet c = getC().apply(sym);
        return new LLSysDecision(game, place, c);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + HL2PGConverter.getHashCode(getPlace());
        int mult = Objects.hashCode(getC());
        hash = 29 * hash * (mult == 0 ? 1 : mult);
        return hash;
    }

    @Override
    public String toDot() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(getPlace().getId()).append(", ");
        sb.append(getC().toDot());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toDot();
    }

}
