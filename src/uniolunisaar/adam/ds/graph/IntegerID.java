package uniolunisaar.adam.ds.graph;

/**
 *
 * @author Manuel Gieseking
 */
public class IntegerID implements StateIdentifier {

    private final int id;

    public IntegerID(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }

}
