package uniolunisaar.adam.ds.highlevel.predicate;

import uniolunisaar.adam.ds.highlevel.terms.ITerm;
import uniolunisaar.adam.ds.highlevel.Valuation;

/**
 *
 * @author Manuel Gieseking
 * @param <TT>
 */
public class BasicPredicate<TT extends IPredicateTerm> implements IPredicate {

    public enum Operator implements IOperator {
        EQ {
            @Override
            public String toSymbol() {
                return "=";
            }
        },
        NEQ {
            @Override
            public String toSymbol() {
                return "!=";
            }
        }
    }

    private final ITerm<TT> t1;
    private final Operator op;
    private final ITerm<TT> t2;

    public BasicPredicate(ITerm<TT> t1, Operator op, ITerm<TT> t2) {
        this.t1 = t1;
        this.op = op;
        this.t2 = t2;
    }

    @Override
    public boolean check(Valuation valuation) {
        TT val1 = t1.getValue(valuation);
        TT val2 = t2.getValue(valuation);
        boolean equals = val1.equals(val2);
        return (op == Operator.EQ) ? equals : !equals;
    }

    @Override
    public String toString() {
        return "(" + t1.toString() + " " + op.toString() + " " + t2.toString() + ")";
    }

    @Override
    public String toSymbol() {
        return "(" + t1.toSymbol() + " " + op.toSymbol() + " " + t2.toSymbol() + ")";
    }
}
