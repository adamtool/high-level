package uniolunisaar.adam.exceptions.highlevel;

import uniol.apt.adt.exception.DatastructureException;

/**
 *
 * @author Manuel Gieseking
 */
public class NoSuchColorClassException extends DatastructureException {

    public static final long serialVersionUID = 0x1l;

    public NoSuchColorClassException(String message) {
        super(message);
    }

    public NoSuchColorClassException(String message, Throwable cause) {
        super(message, cause);
    }

}
