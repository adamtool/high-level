package uniolunisaar.adam.ds.highlevel.predicate;

/**
 *
 * @author Manuel Gieseking
 */
public interface IPredicate {

    public boolean check(Valuation valuation);
    public String toSymbol();

}
