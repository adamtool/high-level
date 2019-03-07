package uniolunisaar.adam.ds.highlevel.predicate;

import java.util.List;
import uniolunisaar.adam.ds.highlevel.Valuation;

/**
 *
 * @author Manuel Gieseking
 */
public class Predicate implements IPredicate {

    public enum Operator implements IOperator {
        AND {
            @Override
            public String toSymbol() {
//                return "⋏"; // this symbol is visualized by dot (within the pdf not the dot file itself) as 180 degree turned.
                return "\u2227";
//" \u22CF " " \u2227 "; 
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

    public static IPredicate createPredicate(List<IPredicate> preds, Operator op) {
        if (preds.isEmpty()) {
            if (op == Operator.AND) {
                return Constants.FALSE;
            } else {
                return Constants.TRUE;
            }
        } else if (preds.size() == 1) {
            return preds.get(0);
        } else {
            IPredicate pred = new Predicate(preds.get(0), op, preds.get(1));
            for (int i = 0; i < preds.size() - 2; i++) {
                pred = new Predicate(pred, op, preds.get(i + 2));
            }
            return pred;
        }
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
