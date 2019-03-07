package uniolunisaar.adam.ds.highlevel.terms;

import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicateTerm;

/**
 *
 * @author Manuel Gieseking
 */
public class Variable implements IPredicateTerm<Color>, IArcTerm<Color>, IArcTupleElement<Color> {

    private final String name;

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Color getValue(Valuation valuation) {
        return valuation.get(this);
    }

    @Override
    public String toSymbol() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
