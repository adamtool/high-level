package uniolunisaar.adam.ds.highlevel.terms;

import uniolunisaar.adam.ds.highlevel.Valuation;

/**
 *
 * @author Manuel Gieseking
 * @param <TT>
 */
public interface ITerm<TT> {

    public TT getValue(Valuation valuation);

    public String toSymbol();
}
