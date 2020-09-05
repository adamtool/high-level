package uniolunisaar.adam.ds.synthesis.highlevel.terms;

import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcType;
import uniolunisaar.adam.ds.synthesis.highlevel.predicate.IPredicateType;

/**
 *
 * @author Manuel Gieseking
 */
public interface IColorType extends IPredicateType, IArcType {

    public String getId();
}
