package uniolunisaar.adam.ds.synthesis.highlevel.terms;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.synthesis.highlevel.predicate.IPredicateTerm;

/**
 *
 * @author Manuel Gieseking
 */
public class Variable implements IPredicateTerm<Color>, IArcTerm<Color>, IArcTupleElement<Color> {

    private final String name;

    public Variable(Variable var) {
        name = var.name;
    }

    public Variable(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> vars = new HashSet<>();
        vars.add(this);
        return vars;
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

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 11 * hash * Objects.hashCode(this.name);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Variable other = (Variable) obj;
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        return true;
    }
}
