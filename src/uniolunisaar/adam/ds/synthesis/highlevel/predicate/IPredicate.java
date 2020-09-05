package uniolunisaar.adam.ds.synthesis.highlevel.predicate;

import java.util.Set;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public interface IPredicate {

    public Set<Variable> getVariables();

    public boolean check(Valuation valuation);

    public String toSymbol();

}
