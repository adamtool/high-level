package uniolunisaar.adam.ds.graph;

import java.util.Objects;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 */
public abstract class AbstractEnvDecision<P, T> implements IDecision<P, T> {

    private final P place;

    public AbstractEnvDecision(P place) {
        this.place = place;
    }

    @Override
    public boolean isEnvDecision() {
        return true;
    }

    @Override
    public P getPlace() {
        return place;
    }

    @Override
    public boolean isChoosen(T t) {
        return true;
    }

    @Override
    public boolean isTop() {
        return false;
    }

    @Override
    public String toDot() {
        return place.toString();
    }

    @Override
    public String toString() {
        return place.toString();
    }

    @Override
    public int hashCode() {
        int hash = 23;
        hash = 29 * hash * Objects.hashCode(this.place);
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
        final AbstractEnvDecision<?, ?> other = (AbstractEnvDecision<?, ?>) obj;
        if (!Objects.equals(this.place, other.place)) {
            return false;
        }
        return true;
    }

}
