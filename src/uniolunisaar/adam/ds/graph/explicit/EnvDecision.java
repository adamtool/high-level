package uniolunisaar.adam.ds.graph.explicit;

import java.util.Objects;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.AbstractEnvDecision;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.petrigame.PetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public class EnvDecision extends AbstractEnvDecision<Place, Transition> implements ILLDecision {

    private final PetriGame game;

    public EnvDecision(PetriGame game, Place place) {
        super(place);
        this.game = game;
    }

    public EnvDecision(EnvDecision dc) {
        // here is a copy of the references OK
        // (as long as no one uses the extensions such that it is not OK)
        super(dc.getPlace());
        this.game = dc.game;
    }

    @Override
    public EnvDecision apply(Symmetry sym) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.    
    }

    @Override
    public int hashCode() {
        int hash = 23;
        hash = 29 * hash * Objects.hashCode(getPlace());
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

    protected PetriGame getGame() {
        return game;
    }

}
