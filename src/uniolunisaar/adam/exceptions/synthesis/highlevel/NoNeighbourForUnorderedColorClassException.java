package uniolunisaar.adam.exceptions.synthesis.highlevel;

import uniol.apt.adt.exception.DatastructureException;

/**
 *
 * @author Manuel Gieseking
 */
public class NoNeighbourForUnorderedColorClassException extends DatastructureException {

    public static final long serialVersionUID = 0x1l;

    public NoNeighbourForUnorderedColorClassException(String message) {
        super(message);
    }

    public NoNeighbourForUnorderedColorClassException(String message, Throwable cause) {
        super(message, cause);
    }

}
