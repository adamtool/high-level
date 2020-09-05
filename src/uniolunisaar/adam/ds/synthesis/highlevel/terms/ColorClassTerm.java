package uniolunisaar.adam.ds.synthesis.highlevel.terms;

import java.util.HashSet;
import java.util.Set;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.synthesis.highlevel.predicate.IPredicateTerm;

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
    public Set<Variable> getVariables() {
        return new HashSet<>();
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

    public String getClassId() {
        return classID;
    }

}
