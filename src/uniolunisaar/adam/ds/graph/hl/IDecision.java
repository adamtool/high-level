package uniolunisaar.adam.ds.graph.hl;

import uniolunisaar.adam.ds.highlevel.ColoredPlace;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 */
public interface IDecision {

    public boolean isEnvDecision();

    public ColoredPlace getPlace();

    public boolean isChoosen(ColoredTransition t);

    public boolean isTop();

    public void apply(Symmetry sym);
    
    public String toDot();
    
}
