package uniolunisaar.adam.ds.graph.hl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import uniol.apt.adt.pn.Transition;

/**
 *
 * @author Manuel Gieseking
 */
public class CommitmentSet {

    public final boolean isTop;
    public final Set<Transition> transitions;

    public CommitmentSet(boolean isTop) {
        this.isTop = isTop;
        transitions = null;
    }

    public CommitmentSet(Transition... transitions) {
        isTop = false;
        this.transitions = new HashSet<>(Arrays.asList(transitions));
    }

    public CommitmentSet(Set<Transition> transitions) {
        isTop = false;
        this.transitions = transitions;
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
        for (Transition transition : this.transitions) {
            if (!other.transitions.contains(transition)) {
                return false;
            }
        }
        for (Transition transition : other.transitions) {
            if (!this.transitions.contains(transition)) {
                return false;
            }
        }

        return true;
    }

}
