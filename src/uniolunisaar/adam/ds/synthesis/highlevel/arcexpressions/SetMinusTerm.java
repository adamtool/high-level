package uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class SetMinusTerm implements IArcTerm<SetMinusType>, IArcTupleElement<SetMinusType> {

    private final ColorClassTerm clazz;
    private final HashSet<Variable> vars;

    public SetMinusTerm(ColorClassTerm clazz, Variable... var) {
        this.clazz = clazz;
        this.vars = new HashSet<>(Arrays.asList(var));
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> varOut = new HashSet<>();
        varOut.addAll(vars);
        return varOut;
    }

    @Override
    public SetMinusType getValue(Valuation valuation) {
        Color[] colors = new Color[vars.size()];
        int i = 0;
        for (Variable var : vars) {
            colors[i++] = var.getValue(valuation);
        }
        return new SetMinusType(clazz.getValue(valuation), colors);
    }

    public ColorClassTerm getClazz() {
        return clazz;
    }

    @Override
    public String toSymbol() {
        StringBuilder sb = new StringBuilder();
        sb.append(clazz.toSymbol()).append("\\{");
        for (Variable var : vars) {
            sb.append(var.toSymbol()).append(",");
        }
        sb.replace(sb.length(), sb.length(), "}");
        return sb.toString();
    }

    @Override
    public String toString() {
        return clazz.toSymbol() + "-" + vars.toString();
    }

}
