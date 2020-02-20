package uniolunisaar.adam.ds.graph.explicit;

import java.util.Objects;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.AbstractSysDecision;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.petrigame.PetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public class SysDecision extends AbstractSysDecision<Place, Transition, CommitmentSet> implements ILLDecision {

    private final PetriGame game;

    /**
     * Copy-Constructor
     *
     * @param dcs
     */
    public SysDecision(SysDecision dcs) {
        super(dcs.getPlace(), new CommitmentSet(dcs.getC()));
        this.game = dcs.game;
    }

    public SysDecision(PetriGame game, Place place, CommitmentSet c) {
        super(place, c);
        this.game = game;
    }

    @Override
    public SysDecision apply(Symmetry sym) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.    
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash * Objects.hashCode(getPlace());
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

    protected PetriGame getGame() {
        return game;
    }

}