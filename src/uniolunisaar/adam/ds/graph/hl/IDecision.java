package uniolunisaar.adam.ds.graph.hl;

import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;

/**
 *
 * @author Manuel Gieseking
 */
public interface IDecision {

    public boolean isEnvDecision();

    public ColoredPlace getPlace();

    public boolean isChoosen(ColoredTransition t);
    
    public boolean isTop();
    
}
