package uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions;

import uniolunisaar.adam.ds.synthesis.highlevel.terms.ITerm;

/**
 *
 * @author Manuel Gieseking
 * @param <T>
 */
public interface IArcTerm<T extends IArcType> extends ITerm<T> {

    public enum Sort {
        VARIABLE,
        SUCCESSOR,
        PREDECESSOR,
        COLORCLASS,
        SETMINUS,
        TUPLE
    }
}
