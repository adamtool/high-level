package uniolunisaar.adam.ds.synthesis.highlevel.terms;

import java.util.Objects;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTupleElementType;

/**
 *
 * @author Manuel Gieseking
 */
public class ColorClassType implements IColorClassType, IArcTupleElementType {

    private final String id;

    public ColorClassType(String id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final ColorClassType other = (ColorClassType) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String getId() {
        return id;
    }

}
