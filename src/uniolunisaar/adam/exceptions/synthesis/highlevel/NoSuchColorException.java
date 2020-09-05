package uniolunisaar.adam.exceptions.synthesis.highlevel;

import uniol.apt.adt.exception.DatastructureException;

/**
 *
 * @author Manuel Gieseking
 */
public class NoSuchColorException extends DatastructureException {

    public static final long serialVersionUID = 0x1l;

    public NoSuchColorException(String message) {
        super(message);
    }

    public NoSuchColorException(String message, Throwable cause) {
        super(message, cause);
    }

}
