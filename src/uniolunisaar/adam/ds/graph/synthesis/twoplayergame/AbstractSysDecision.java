package uniolunisaar.adam.ds.graph.synthesis.twoplayergame;

import java.util.Objects;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <C>
 */
public abstract class AbstractSysDecision<P, T, C extends AbstractCommitmentSet<T>> implements IDecision<P, T> {

    private final P place;
//    private final boolean type;
    private final C c;

//    public SysDecision(Place place, ColorToken color, boolean type, CommitmentSet c) {
//        this.place = place;
//        this.color = color;
//        this.type = type;
//        this.c = c;
//    }
    public AbstractSysDecision(P place, C c) {
        this.place = place;
        this.c = c;
    }

    @Override
    public boolean isEnvDecision() {
        return false;
    }

    @Override
    public P getPlace() {
        return place;
    }

    @Override
    public boolean isChoosen(T t) {
        return c.isChoosen(t);
    }

    @Override
    public boolean isTop() {
        return c.isTop();
    }

//    public boolean isType() {
//        return type;
//    }
    protected C getC() {
        return c;
    }
    
    @Override
    public String toDot() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(place.toString()).append(", ");
        sb.append(c.toDot());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(place.toString()).append(", ");
        sb.append(c.toDot());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.place);
        hash = 53 * hash + Objects.hashCode(this.c);
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
        final AbstractSysDecision<?, ?, ?> other = (AbstractSysDecision<?, ?, ?>) obj;
        if (!Objects.equals(this.place, other.place)) {
            return false;
        }
        if (!Objects.equals(this.c, other.c)) {
            return false;
        }
        return true;
    }

}
