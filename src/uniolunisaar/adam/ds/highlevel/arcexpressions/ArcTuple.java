package uniolunisaar.adam.ds.highlevel.arcexpressions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassTerm;
import uniolunisaar.adam.ds.highlevel.terms.PredecessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class ArcTuple implements IArcTerm<IArcTupleType> {

    private final ArrayList<Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>>> tuple;

    public ArcTuple() {
        tuple = new ArrayList<>();
    }

    public ArcTuple(Variable... vars) {
        this();
        for (int i = 0; i < vars.length; i++) {
            tuple.add(new Pair<>(IArcTupleElement.Sort.VARIABLE, vars[i]));
        }
    }

    public ArcTuple(SuccessorTerm... succs) {
        this();
        for (int i = 0; i < succs.length; i++) {
            tuple.add(new Pair<>(IArcTupleElement.Sort.SUCCESSOR, succs[i]));
        }
    }

    public ArcTuple(PredecessorTerm... pres) {
        this();
        for (int i = 0; i < pres.length; i++) {
            tuple.add(new Pair<>(IArcTupleElement.Sort.PREDECESSOR, pres[i]));
        }
    }

    public ArcTuple(ColorClassTerm... colorClasses) {
        this();
        for (int i = 0; i < colorClasses.length; i++) {
            tuple.add(new Pair<>(IArcTupleElement.Sort.COLORCLASS, colorClasses[i]));
        }
    }

    public ArcTuple(SetMinusTerm... setminusTerms) {
        this();
        for (int i = 0; i < setminusTerms.length; i++) {
            tuple.add(new Pair<>(IArcTupleElement.Sort.SETMINUS, setminusTerms[i]));
        }
    }

    public Collection<Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>>> getValues() {
        return Collections.unmodifiableCollection(tuple);
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> vars = new HashSet<>();
        for (Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>> elem : tuple) {
            vars.addAll(elem.getSecond().getVariables());
        }
        return vars;
    }

    public boolean add(Variable... vars) {
        boolean ret = true;
        for (int i = 0; i < vars.length; i++) {
            ret &= tuple.add(new Pair<>(IArcTupleElement.Sort.VARIABLE, vars[i]));
        }
        return ret;
    }

    public boolean add(SuccessorTerm... succs) {
        boolean ret = true;
        for (int i = 0; i < succs.length; i++) {
            ret &= tuple.add(new Pair<>(IArcTupleElement.Sort.SUCCESSOR, succs[i]));
        }
        return ret;
    }

    public boolean add(PredecessorTerm... pres) {
        boolean ret = true;
        for (int i = 0; i < pres.length; i++) {
            ret &= tuple.add(new Pair<>(IArcTupleElement.Sort.PREDECESSOR, pres[i]));
        }
        return ret;
    }

    public boolean add(ColorClassTerm... colorClasses) {
        boolean ret = true;
        for (int i = 0; i < colorClasses.length; i++) {
            ret &= tuple.add(new Pair<>(IArcTupleElement.Sort.COLORCLASS, colorClasses[i]));
        }
        return ret;
    }

    public boolean add(SetMinusTerm... setminusTerms) {
        boolean ret = true;
        for (int i = 0; i < setminusTerms.length; i++) {
            ret &= tuple.add(new Pair<>(IArcTupleElement.Sort.SETMINUS, setminusTerms[i]));
        }
        return ret;
    }

    @Override
    public IArcTupleType getValue(Valuation valuation) {
        ArcTupleType tupleType = new ArcTupleType();
        for (int i = 0; i < tuple.size(); i++) {
            IArcTupleElement<? extends IArcTupleElementType> elem = tuple.get(i).getSecond();
            IArcTupleElementType val = elem.getValue(valuation);
            Pair<IArcTupleElement.Sort, IArcTupleElementType> pair = new Pair<>(tuple.get(i).getFirst(), val);
            tupleType.add(pair);
        }
        return tupleType;
    }

    @Override
    public String toSymbol() {
        return toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < tuple.size() - 1; i++) {
            sb.append(tuple.get(i).getSecond().toString()).append(",");
        }
        if (tuple.size() >= 1) {
            sb.append(tuple.get(tuple.size() - 1).getSecond().toString());
        }
        sb.append(")");
        return sb.toString();
    }

}
