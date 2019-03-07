package uniolunisaar.adam.ds.highlevel.terms;

import java.util.Set;
import uniolunisaar.adam.ds.highlevel.Valuation;

/**
 *
 * @author Manuel Gieseking
 * @param <TT>
 */
public interface ITerm<TT> {

    public Set<Variable> getVariables();

    public TT getValue(Valuation valuation);

    public String toSymbol();
}
