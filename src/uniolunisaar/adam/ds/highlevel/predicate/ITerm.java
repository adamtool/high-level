package uniolunisaar.adam.ds.highlevel.predicate;

/**
 *
 * @author Manuel Gieseking
 * @param <TT>
 */
public interface ITerm<TT extends ITermType> {

    public TT getValue(Valuation valuation);

    public String toSymbol();
}
