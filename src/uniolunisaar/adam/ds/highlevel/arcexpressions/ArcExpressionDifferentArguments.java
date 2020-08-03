package uniolunisaar.adam.ds.highlevel.arcexpressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.PredecessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 * This class allows to have a sum of different objects (Variable, Colorclass,
 * Successors, Tuples) as arc expression. This is not possible for well-formed
 * nets.
 *
 * @author Manuel Gieseking
 */
public class ArcExpressionDifferentArguments {

    private final ArrayList<Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>>> expressions;

    public ArcExpressionDifferentArguments() {
        expressions = new ArrayList<>();
    }

    public ArcExpressionDifferentArguments(Variable x) {
        this();
        expressions.add(new Pair<>(IArcTerm.Sort.VARIABLE, x));
    }

    public ArcExpressionDifferentArguments(SuccessorTerm succ) {
        this();
        expressions.add(new Pair<>(IArcTerm.Sort.SUCCESSOR, succ));
    }

    public ArcExpressionDifferentArguments(PredecessorTerm pre) {
        this();
        expressions.add(new Pair<>(IArcTerm.Sort.PREDECESSOR, pre));
    }

    public ArcExpressionDifferentArguments(ColorClassTerm colorClass) {
        this();
        expressions.add(new Pair<>(IArcTerm.Sort.COLORCLASS, colorClass));
    }

    public ArcExpressionDifferentArguments(ArcTuple tuple) {
        this();
        expressions.add(new Pair<>(IArcTerm.Sort.TUPLE, tuple));
    }

    public boolean add(Variable x) {
        return expressions.add(new Pair<>(IArcTerm.Sort.VARIABLE, x));
    }

    public boolean add(SuccessorTerm succ) {
        return expressions.add(new Pair<>(IArcTerm.Sort.SUCCESSOR, succ));
    }

    public boolean add(PredecessorTerm pre) {
        return expressions.add(new Pair<>(IArcTerm.Sort.PREDECESSOR, pre));
    }

    public boolean add(ColorClassTerm colorClass) {
        return expressions.add(new Pair<>(IArcTerm.Sort.COLORCLASS, colorClass));
    }

    public boolean add(ArcTuple tuple) {
        return expressions.add(new Pair<>(IArcTerm.Sort.TUPLE, tuple));
    }

    public Set<Variable> getVariables() {
        Set<Variable> vars = new HashSet<>();
        for (Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> expression : expressions) {
            vars.addAll(expression.getSecond().getVariables());
        }
        return vars;
    }

    public Collection<Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>>> getExpresssions() {
        return Collections.unmodifiableCollection(expressions);
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
