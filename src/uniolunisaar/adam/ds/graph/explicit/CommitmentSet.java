package uniolunisaar.adam.ds.graph.explicit;

import java.util.Objects;
import java.util.Set;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.AbstractCommitmentSet;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;

/**
 *
 * @author Manuel Gieseking
 */
public class CommitmentSet extends AbstractCommitmentSet<Transition> {

    private final PetriGameWithTransits game;

    public CommitmentSet(PetriGameWithTransits game, boolean isTop) {
        super(isTop);
        this.game = game;
    }

    /**
     * Attention this uses just the references of the transitions of the given
     * list and the list itself. Don't change them afterward, they are saved as
     * HashSet and thus contains would not longer work!
     *
     * @param game
     * @param transitions
     */
    public CommitmentSet(PetriGameWithTransits game, Transition... transitions) {
        super(transitions);
        this.game = game;
    }

    /**
     * Attention this uses just the references of the transitions of the given
     * list and the list itself. Don't change them afterward, they are saved as
     * HashSet and thus contains would not longer work!
     *
     * @param game
     * @param transitions
     */
    public CommitmentSet(PetriGameWithTransits game, Set<Transition> transitions) {
        super(transitions);
        this.game = game;
    }

    public CommitmentSet(CommitmentSet c) {
        super(c.isTop(), c.getTransitions());
        this.game = c.game;
    }

    @Override
    public CommitmentSet apply(Symmetry sym) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + (isTop() ? 1 : 0);
        int tr = 1;
        if (getTransitions() != null) {
            for (Transition transition : getTransitions()) {
                tr *= Objects.hashCode(transition);
            }
        }
        hash = 13 * hash * tr;
        return hash;
    }

    @Override
    public String toDot() {
        StringBuilder sb = new StringBuilder();
        if (isTop()) {
            sb.append("T");
        } else {
            sb.append("{");
            for (Transition transition : getTransitions()) {
                sb.append(transition.getId()).append(",");
            }
            if (getTransitions().size() >= 1) {
                sb.delete(sb.length() - 1, sb.length());
            }
            sb.append("}");
        }
        return sb.toString();
    }

    protected PetriGameWithTransits getGame() {
        return game;
    }
}
