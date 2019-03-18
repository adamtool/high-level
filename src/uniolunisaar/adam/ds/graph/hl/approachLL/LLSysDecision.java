package uniolunisaar.adam.ds.graph.hl.approachLL;

import java.util.ArrayList;
import java.util.List;
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

    public LLSysDecision(PetriGame game, LLSysDecision dcs) {
        super(dcs.place, new LLCommitmentSet(game, dcs.c));
        this.game = game;
    }

    public LLSysDecision(PetriGame game, Place place, LLCommitmentSet c) {
        super(place, c);
        this.game = game;
    }

    @Override
    public void apply(Symmetry sym) {
        // apply it to the place
// old version        
//        String id = HL2PGConverter.getHLPlaceID(place.getId());
//        String[] col = HL2PGConverter.getPlaceColorIDs(place.getId());
//        List<Color> colors = new ArrayList<>();
//        for (int i = 0; i < col.length - 1; i++) {
//            colors.add(sym.get(new Color(col[i])));
//        }
        String id = HL2PGConverter.getOrigID(place);
        List<Color> col = HL2PGConverter.getColors(place);
        List<Color> colors = new ArrayList<>();
        for (int i = 0; i < col.size(); i++) {
            colors.add(sym.get(col.get(i)));
        }
        place = game.getPlace(HL2PGConverter.getPlaceID(id, colors));
        // apply it to the commitment set
        c.apply(sym);
    }

}
