package uniolunisaar.adam.ds.graph.synthesis.twoplayergame;

import java.util.Objects;
import uniol.apt.adt.extension.Extensible;

/**
 *
 * @author Manuel Gieseking
 * @param <T>
 * @param <S>
 */
public class GameGraphFlow<T, S extends StateIdentifier> extends Extensible {

    private final S source;
    private final S target;
    private final T transition;

    public GameGraphFlow(S source, T transition, S target) {
        this.source = source;
        this.target = target;
        this.transition = transition;
    }

    public S getSource() {
        return source;
    }

    public S getTarget() {
        return target;
    }

    public T getTransition() {
        return transition;
    }

    @Override
    public String toString() {
        return source.toString() + "->" + target.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + Objects.hashCode(this.source);
        hash = 59 * hash + Objects.hashCode(this.target);
        hash = 59 * hash + Objects.hashCode(this.transition);
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
        final GameGraphFlow<?, ?> other = (GameGraphFlow<?, ?>) obj;
        if (!Objects.equals(this.transition, other.transition)) {
            return false;
        }
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        return true;
    }

}
