package uniolunisaar.adam.ds.synthesis.highlevel;

import java.util.Objects;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.IColorType;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTupleElementType;

/**
 *
 * @author Manuel Gieseking
 */
public class Color implements IColorType, IArcTupleElementType {

    private final String id;

    public Color(Color c) {
        id = c.id;
    }

    public Color(String id) {
        this.id = id;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 13 * hash * Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Color other = (Color) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id;
    }

}
