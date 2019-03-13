package uniolunisaar.adam.ds.highlevel.arcexpressions;

import java.util.HashSet;
import java.util.Set;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class SetMinusTerm implements IArcTerm<SetMinusType>, IArcTupleElement<SetMinusType> {

    private final ColorClassTerm clazz;
    private final Variable var;

    public SetMinusTerm(ColorClassTerm clazz, Variable var) {
        this.clazz = clazz;
        this.var = var;
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> vars = new HashSet<>();
        vars.add(var);
        return vars;
    }

    @Override
    public SetMinusType getValue(Valuation valuation) {
        return new SetMinusType(clazz.getValue(valuation), var.getValue(valuation));
    }

    public ColorClassTerm getClazz() {
        return clazz;
    }

    public Variable getVariable() {
        return var;
    }

    @Override
    public String toSymbol() {
        return clazz.toSymbol() + "\\" + var.toSymbol();
    }

    @Override
    public String toString() {
        return clazz.toSymbol() + "-" + var.toSymbol();
    }

}
