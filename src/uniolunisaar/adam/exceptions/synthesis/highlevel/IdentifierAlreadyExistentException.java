package uniolunisaar.adam.exceptions.synthesis.highlevel;

import uniol.apt.adt.exception.DatastructureException;

/**
 *
 * @author Manuel Gieseking
 */
public class IdentifierAlreadyExistentException extends DatastructureException {

    public static final long serialVersionUID = 0x1l;

    public IdentifierAlreadyExistentException(String message) {
        super(message);
    }

    public IdentifierAlreadyExistentException(String message, Throwable cause) {
        super(message, cause);
    }

}
