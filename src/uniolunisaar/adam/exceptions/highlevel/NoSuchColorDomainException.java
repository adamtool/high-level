package uniolunisaar.adam.exceptions.highlevel;

import uniol.apt.adt.exception.DatastructureException;

/**
 *
 * @author Manuel Gieseking
 */
public class NoSuchColorDomainException extends DatastructureException {

    public static final long serialVersionUID = 0x1l;

    public NoSuchColorDomainException(String message) {
        super(message);
    }

    public NoSuchColorDomainException(String message, Throwable cause) {
        super(message, cause);
    }

}
