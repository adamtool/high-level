package uniolunisaar.adam.ds.highlevel.terms;

import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.Color;

/**
 *
 * @author Manuel Gieseking
 */
public class Variable implements ITerm<Color> {

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
