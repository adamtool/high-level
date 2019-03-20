package uniolunisaar.adam.ds.graph.hl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 * @param <T>
 */
public abstract class CommitmentSet<T> {

    private final boolean isTop;
    private final Set<T> transitions;

    public CommitmentSet(boolean isTop) {
        this.isTop = isTop;
        transitions = null;
    }

    /**
     * Attention this uses just the references of the transitions of the given
     * list and the list itself. Don't change them afterward, they are saved as
     * HashSet and thus contains would not longer work!
     *
     * @param transitions
     */
    public CommitmentSet(T... transitions) {
        isTop = false;
        this.transitions = new HashSet<>(Arrays.asList(transitions));
    }

    /**
     * Attention this uses just the references of the transitions of the given
     * list and the list itself. Don't change them afterward, they are saved as
     * HashSet and thus contains would not longer work!
     *
     * @param transitions
     */
    public CommitmentSet(Set<T> transitions) {
        isTop = false;
        this.transitions = transitions;
    }

    /**
     * The pendant of the copying constructor.
     *
     * @param isTop
     * @param transitions
     */
    protected CommitmentSet(boolean isTop, Set<T> transitions) {
        this.isTop = isTop;
        if (isTop) {
            this.transitions = null;
        } else {
            // here is a copy of the references OK
            // (as long as no one uses the extensions such that it is not OK)
            this.transitions = new HashSet<>(transitions);
        }
    }

    public boolean isChoosen(T t) {
        if (isTop) {
            return false;
        }
//        System.out.println(transitions.toString());
//        System.out.println(t.toString());
//        System.out.println("contains" + transitions.contains(t));
//        for (ColoredTransition transition : transitions) {
//            if (transition.equals(t)) {
//                System.out.println(transition.hashCode());
//                System.out.println(t.hashCode());
//            } else {
//                System.out.println("false");
//            }
//        }
        return transitions.contains(t);
    }

//    @Deprecated
//    public abstract void apply(Symmetry sym);
    public abstract CommitmentSet<T> apply(Symmetry sym);

    public String toDot() {
        StringBuilder sb = new StringBuilder();
        if (isTop) {
            sb.append("T");
        } else {
            sb.append("{");
            for (T transition : transitions) {
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

    /**
     * Attention this returns the references of the transitions. Don't change
     * the elements, since contains won't longer work!
     *
     * @return
     */
    protected Set<T> getTransitions() {
        return transitions;
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
        final CommitmentSet<T> other = (CommitmentSet<T>) obj;
        if (this.isTop != other.isTop) {
            return false;
        }
        // todo: do it better?
//        if (this.transitions == null && other.transitions == null) {
//            return true;
//        }
//        if ((this.transitions != null && other.transitions == null) || (this.transitions == null && other.transitions != null)) {
//            return false;
//        }
//        for (T transition : this.transitions) {
//            if (!other.transitions.contains(transition)) {
//                return false;
//            }
//        }
//        for (T transition : other.transitions) {
//            if (!this.transitions.contains(transition)) {
//                return false;
//            }
//        }
        if (!Objects.equals(this.transitions, other.transitions)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "CommitmentSet{" + "isTop=" + isTop + ", transitions=" + transitions + '}';
    }

}
