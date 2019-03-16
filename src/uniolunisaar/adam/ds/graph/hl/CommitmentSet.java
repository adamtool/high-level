package uniolunisaar.adam.ds.graph.hl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 */
public class CommitmentSet {

    private final boolean isTop;
    private final Set<ColoredTransition> transitions;

    public CommitmentSet(CommitmentSet c) {
        isTop = c.isTop;
        if (isTop) {
            transitions = null;
        } else {
            transitions = new HashSet<>();
            for (ColoredTransition transition : c.transitions) {
                transitions.add(new ColoredTransition(transition));
            }
        }
    }

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
        if (isTop) {
            return false;
        }
        return transitions.contains(t);
    }

    public void apply(Symmetry sym) {
        if (isTop) {
            return;
        }
        for (ColoredTransition transition : transitions) {
            transition.apply(sym);
        }
    }

    public String toDot() {
        StringBuilder sb = new StringBuilder();
        if (isTop) {
            sb.append("T");
        } else {
            sb.append("{");
            for (ColoredTransition transition : transitions) {
                sb.append(transition.toString()).append(",");
            }
            if (transitions.size() >= 1) {
                sb.delete(sb.length() - 1, sb.length());
            }
            sb.append("}");
        }
        return sb.toString();
    }

    public boolean isTop() {
        return isTop;
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
