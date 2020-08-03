package uniolunisaar.adam.ds.highlevel.arcexpressions;

/**
 *
 * @author Manuel Gieseking
 * @param <T>
 */
public interface IArcTupleElement<T extends IArcType> extends IArcTerm<T> {

    public enum Sort {
        VARIABLE,
        SUCCESSOR,
        PREDECESSOR,
        COLORCLASS,
        SETMINUS
    }
}
