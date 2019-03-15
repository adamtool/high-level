package uniolunisaar.adam.ds.graph.hl;

import uniol.apt.adt.extension.Extensible;

/**
 * Rudimentary class for a state of the finite graph.
 *
 * This class only provides an id of the state and the possible to add extension
 * to it.
 *
 * @author Manuel Gieseking
 */
//public class SRGState extends Extensible {
public abstract class SRGState extends Extensible {

//    private long id = -1;
    /**
     * Constructor.
     *
     * Initially the id is set to -1.
     */
    public SRGState() {
    }
//
//    /**
//     * Sets the id of the state to the given id.
//     *
//     * @param id - the id to set for the state.
//     */
//    void setId(long id) {
//        this.id = id;
//    }
//
//    /**
//     * Returns the id of this state.
//     *
//     * @return - the id of the state.
//     */
//    public long getId() {
//        return id;
//    }

    public abstract int getId();
}
