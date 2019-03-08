package uniolunisaar.adam.ds.highlevel.predicate;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

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
                return Constants.TRUE;
            } else {
                return Constants.FALSE;
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
    public Set<Variable> getVariables() {
        Set<Variable> vars = new HashSet<>();
        vars.addAll(p1.getVariables());
        vars.addAll(p2.getVariables());
        return vars;
    }

    @Override
    public boolean check(Valuation valuation) {
        boolean res1 = p1.check(valuation);
        boolean res2 = p2.check(valuation);
        return op == Operator.AND ? res1 && res2 : res1 || res2;
    }

    @Override
    public String toString() {
        IPredicate lost = op.equals(Operator.AND) ? Constants.TRUE : Constants.FALSE;
        String operator = p1.equals(lost) || p2.equals(lost) ? "" : op.toString();
        return "(" + p1.toString() + " " + operator + " " + p2.toString() + ")";
    }

    @Override
    public String toSymbol() {
        IPredicate lost = op.equals(Operator.AND) ? Constants.TRUE : Constants.FALSE;
        String operator = p1.equals(lost) || p2.equals(lost) ? "" : op.toSymbol();
        return "(" + p1.toSymbol() + " " + operator + " " + p2.toSymbol() + ")";
    }

}
