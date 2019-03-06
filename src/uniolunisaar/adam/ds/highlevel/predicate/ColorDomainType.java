package uniolunisaar.adam.ds.highlevel.predicate;

import java.util.Objects;

/**
 *
 * @author Manuel Gieseking
 */
public class ColorDomainType implements ITermType {

    private final String id;

    public ColorDomainType(String id) {
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
        final ColorDomainType other = (ColorDomainType) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
