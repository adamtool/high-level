package uniolunisaar.adam.ds.highlevel.terms;

import uniolunisaar.adam.ds.highlevel.Valuation;

/**
 *
 * @author Manuel Gieseking
 */
public class ColorClassTerm implements ITerm<ColorClassType> {

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
