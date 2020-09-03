package uniolunisaar.adam.ds.highlevel.predicate;

import java.util.Set;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class UnaryPredicate implements IPredicate {

    public enum Operator implements IOperator {
        NEG {
            @Override
            public String toSymbol() {
                return "¬";// "\u00AC";
            }
        }
    }

    private final IPredicate p1;
    private final Operator op;

    public UnaryPredicate(Operator op, IPredicate p1) {
        this.p1 = p1;
        this.op = op;
    }

    @Override
    public Set<Variable> getVariables() {
        return p1.getVariables();
    }

    @Override
    public boolean check(Valuation valuation) {
        return !p1.check(valuation);
    }

    @Override
    public String toString() {
        return (p1 == Constants.TRUE) ? Constants.FALSE.toString() : (p1 == Constants.FALSE) ? Constants.TRUE.toString() : op.toString() + " " + p1.toString();
    }

    @Override
    public String toSymbol() {
        return (p1 == Constants.TRUE) ? Constants.FALSE.toSymbol() : (p1 == Constants.FALSE) ? Constants.TRUE.toSymbol() : op.toSymbol() + " " + p1.toSymbol();
    }

    public Operator getOperator() {
        return op;
    }

    public IPredicate getOperand() {
        return p1;
    }
}
