package uniolunisaar.adam.ds.highlevel.predicate;

import uniolunisaar.adam.ds.highlevel.Valuation;

/**
 *
 * @author Manuel Gieseking
 */
public interface IPredicate {

    public boolean check(Valuation valuation);
    public String toSymbol();

}
