package uniolunisaar.adam.exceptions.highlevel;

import uniol.apt.adt.exception.DatastructureException;

/**
 *
 * @author Manuel Gieseking
 */
public class NoSuccessorForUnorderedColorClassException extends DatastructureException {

    public static final long serialVersionUID = 0x1l;

    public NoSuccessorForUnorderedColorClassException(String message) {
        super(message);
    }

    public NoSuccessorForUnorderedColorClassException(String message, Throwable cause) {
        super(message, cause);
    }

}
