package uniolunisaar.adam.ds.highlevel;

import java.util.Objects;
import uniolunisaar.adam.ds.highlevel.terms.IColorType;

/**
 *
 * @author Manuel Gieseking
 */
public class Color implements IColorType {

    private final String id;

    public Color(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
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
        final Color other = (Color) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
