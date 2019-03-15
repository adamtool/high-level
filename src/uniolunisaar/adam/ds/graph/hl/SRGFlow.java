package uniolunisaar.adam.ds.graph.hl;

import java.util.Objects;
import uniol.apt.adt.extension.Extensible;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;

/**
 *
 * @author Manuel Gieseking
 */
public class SRGFlow extends Extensible {

    private final int sourceid;
    private final int targetid;
    private final ColoredTransition transition;

    public SRGFlow(int sourceid, ColoredTransition transition, int targetid) {
        this.sourceid = sourceid;
        this.targetid = targetid;
        this.transition = transition;
    }

    public int getSourceid() {
        return sourceid;
    }

    public int getTargetid() {
        return targetid;
    }

    public ColoredTransition getTransition() {
        return transition;
    }

    @Override
    public String toString() {
        return sourceid + "->" + targetid;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 59 * hash + this.sourceid;
        hash = 59 * hash + this.targetid;
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
        final SRGFlow other = (SRGFlow) obj;
        if (this.sourceid != other.sourceid) {
            return false;
        }
        if (this.targetid != other.targetid) {
            return false;
        }
        if (!Objects.equals(this.transition, other.transition)) {
            return false;
        }
        return true;
    }

}
