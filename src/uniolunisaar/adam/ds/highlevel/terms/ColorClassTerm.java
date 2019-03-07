package uniolunisaar.adam.ds.highlevel.terms;

import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicateTerm;

/**
 *
 * @author Manuel Gieseking
 */
public class ColorClassTerm implements IPredicateTerm<ColorClassType>, IArcTerm<ColorClassType>, IArcTupleElement<ColorClassType> {

    private final String classID;

    public ColorClassTerm(String classID) {
        this.classID = classID;
    }

    @Override
    public ColorClassType getValue(Valuation valuation) {
        return new ColorClassType(classID);
    }

    @Override
    public String toSymbol() {
        return classID;
    }

    @Override
    public String toString() {
        return classID;
    }

}
