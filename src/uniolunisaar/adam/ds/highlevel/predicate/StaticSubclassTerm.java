package uniolunisaar.adam.ds.highlevel.predicate;

/**
 *
 * @author Manuel Gieseking
 */
public class StaticSubclassTerm implements ITerm<ColorDomainType> {

    private final String staticSubclassID;

    public StaticSubclassTerm(String staticSubclassID) {
        this.staticSubclassID = staticSubclassID;
    }

    @Override
    public ColorDomainType getValue(Valuation valuation) {
        return new ColorDomainType(staticSubclassID);
    }

    @Override
    public String toSymbol() {
        return staticSubclassID;
    }

    @Override
    public String toString() {
        return staticSubclassID;
    }

}
