package uniolunisaar.adam.ds.highlevel.predicate;

/**
 *
 * @author Manuel Gieseking
 */
public class Predicate implements IPredicate {

    public enum Operator implements IOperator {
        AND {
            @Override
            public String toSymbol() {
                return "⋏"; //" \u22CF " " \u2227 ";
            }
        },
        OR {
            @Override
            public String toSymbol() {
                return "⋎"; //" \u22CE " " \u2228 "
            }
        }
    }

    private final IPredicate p1;
    private final Operator op;
    private final IPredicate p2;

    public Predicate(IPredicate p1, Operator op, IPredicate p2) {
        this.p1 = p1;
        this.op = op;
        this.p2 = p2;
    }

    @Override
    public boolean check(Valuation valuation) {
        boolean res1 = p1.check(valuation);
        boolean res2 = p2.check(valuation);
        return op == Operator.AND ? res1 && res2 : res1 || res2;
    }

    @Override
    public String toString() {
        return "(" + p1.toString() + " " + op.toString() + " " + p2.toString() + ")";
    }

    @Override
    public String toSymbol() {
        return "(" + p1.toSymbol() + " " + op.toSymbol() + " " + p2.toSymbol() + ")";
    }

}
