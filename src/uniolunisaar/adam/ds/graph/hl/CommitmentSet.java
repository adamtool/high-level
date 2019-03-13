package uniolunisaar.adam.ds.graph.hl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;

/**
 *
 * @author Manuel Gieseking
 */
public class CommitmentSet {

    public final boolean isTop;
    public final Set<ColoredTransition> transitions;

    public CommitmentSet(boolean isTop) {
        this.isTop = isTop;
        transitions = null;
    }

    public CommitmentSet(ColoredTransition... transitions) {
        isTop = false;
        this.transitions = new HashSet<>(Arrays.asList(transitions));
    }

    public CommitmentSet(Set<ColoredTransition> transitions) {
        isTop = false;
        this.transitions = transitions;
    }

    public boolean isChoosen(ColoredTransition t) {
        return transitions.contains(t);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (this.isTop ? 1 : 0);
        hash = 59 * hash + Objects.hashCode(this.transitions);
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
        final CommitmentSet other = (CommitmentSet) obj;
        if (this.isTop != other.isTop) {
            return false;
        }
        // todo: do it better?
        if (this.transitions == null && other.transitions == null) {
            return true;
        }
        if ((this.transitions != null && other.transitions == null) || (this.transitions == null && other.transitions != null)) {
            return false;
        }
        for (ColoredTransition transition : this.transitions) {
            if (!other.transitions.contains(transition)) {
                return false;
            }
        }
        for (ColoredTransition transition : other.transitions) {
            if (!this.transitions.contains(transition)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public String toString() {
        return "CommitmentSet{" + "isTop=" + isTop + ", transitions=" + transitions + '}';
    }

}
