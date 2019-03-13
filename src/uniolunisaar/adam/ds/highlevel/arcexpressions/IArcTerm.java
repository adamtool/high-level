package uniolunisaar.adam.ds.highlevel.arcexpressions;

import uniolunisaar.adam.ds.highlevel.terms.ITerm;

/**
 *
 * @author Manuel Gieseking
 * @param <T>
 */
public interface IArcTerm<T extends IArcType> extends ITerm<T> {

    public enum Sort {
        VARIABLE,
        SUCCESSOR,
        COLORCLASS,
        SETMINUS,
        TUPLE
    }
}
