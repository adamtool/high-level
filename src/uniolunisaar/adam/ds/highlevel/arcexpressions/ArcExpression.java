package uniolunisaar.adam.ds.highlevel.arcexpressions;

import java.util.ArrayList;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class ArcExpression {

    private final ArrayList<Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>>> expressions;

    public ArcExpression() {
        expressions = new ArrayList<>();
    }

    public ArcExpression(Variable x) {
        this();
        expressions.add(new Pair<>(IArcTerm.Sort.VARIABLE, x));
    }

    public ArcExpression(SuccessorTerm succ) {
        this();
        expressions.add(new Pair<>(IArcTerm.Sort.SUCCESSOR, succ));
    }

    public ArcExpression(ColorClassTerm colorClass) {
        this();
        expressions.add(new Pair<>(IArcTerm.Sort.COLORCLASS, colorClass));
    }

    public ArcExpression(ArcTuple tuple) {
        this();
        expressions.add(new Pair<>(IArcTerm.Sort.TUPLE, tuple));
    }

    public boolean add(Variable x) {
        return expressions.add(new Pair<>(IArcTerm.Sort.VARIABLE, x));
    }

    public boolean add(SuccessorTerm succ) {
        return expressions.add(new Pair<>(IArcTerm.Sort.SUCCESSOR, succ));
    }

    public boolean add(ColorClassTerm colorClass) {
        return expressions.add(new Pair<>(IArcTerm.Sort.COLORCLASS, colorClass));
    }

    public boolean add(ArcTuple tuple) {
        return expressions.add(new Pair<>(IArcTerm.Sort.TUPLE, tuple));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < expressions.size() - 1; i++) {
            sb.append(expressions.get(i).getSecond().toString()).append("+");
        }
        if (expressions.size() >= 1) {
            sb.append(expressions.get(expressions.size() - 1).getSecond().toString());
        }
        return sb.toString();
    }

}
