package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit;

import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.IDecision;

/**
 *
 * @author Manuel Gieseking
 */
public interface ILLDecision extends IDecision<Place, Transition> {

    public String getIDChain();
}
