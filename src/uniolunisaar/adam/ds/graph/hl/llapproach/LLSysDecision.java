package uniolunisaar.adam.ds.graph.hl.llapproach;

import uniolunisaar.adam.ds.graph.explicit.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import uniol.apt.adt.pn.Place;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.pg.converter.hl.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class LLSysDecision extends SysDecision {

    public LLSysDecision(LLSysDecision dcs) {
        super(dcs);
    }

    public LLSysDecision(SysDecision dcs) {
        super(dcs);
    }

    public LLSysDecision(PetriGameWithTransits game, Place place, LLCommitmentSet c) {
        super(game, place, c);
    }

    public LLSysDecision(PetriGameWithTransits game, Place place, CommitmentSet c) {
        super(game, place, c);
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
        Place place = getGame().getPlace(HL2PGConverter.getPlaceID(id, colors));
        // apply it to the commitment set
        LLCommitmentSet c = ((LLCommitmentSet) getC()).apply(sym);
        return new LLSysDecision(getGame(), place, c);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash * HL2PGConverter.getHashCode(getPlace());
        int mult = Objects.hashCode(getC());
        hash = 29 * hash * (mult == 0 ? 1 : mult);
        return hash;
    }
}
