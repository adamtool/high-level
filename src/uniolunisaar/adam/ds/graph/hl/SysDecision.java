package uniolunisaar.adam.ds.graph.hl;

import java.util.Objects;
import uniolunisaar.adam.logic.hl.SGGBuilder;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <C>
 */
public abstract class SysDecision<P, T, C extends CommitmentSet<T>> implements IDecision<P, T> {

    protected P place;
//    private final boolean type;
    protected C c;

//    public SysDecision(Place place, ColorToken color, boolean type, CommitmentSet c) {
//        this.place = place;
//        this.color = color;
//        this.type = type;
//        this.c = c;
//    }
    public SysDecision(P place, C c) {
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
    public C getC() {
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
    public int hashCode() {
        int hash = 5;
        hash = 29 * hash + Objects.hashCode(this.place);
        hash = 29 * hash * Objects.hashCode(this.c);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (SGGBuilder.depth < 10) {
            System.out.println("used sys equals");
        }
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            if (SGGBuilder.depth < 10) {
                System.out.println("used");
            }
            return false;
        }
        if (getClass() != obj.getClass()) {
            if (SGGBuilder.depth < 10) {
                System.out.println("COMMITMENT");
            }
            return false;
        }
        final SysDecision other = (SysDecision) obj;
        if (!Objects.equals(this.place, other.place)) {
            if (SGGBuilder.depth < 10) {
                System.out.println(this.place);
            }
            return false;
        }
        if (!Objects.equals(this.c, other.c)) {
            if (SGGBuilder.depth < 10) {
                System.out.println("COMMITMENT");
            }
            return false;
        }
        if (SGGBuilder.depth < 10) {
            System.out.println("out is true");
        }
        return true;
    }

}
