package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractCommitmentSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps.LexiLLTransitionIDWithoutColorComparator;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps.LexiTransitionIDComparator;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
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

    /**
     * Returns a concatenated String of all IDs of the commitment set. If the
     * set is ordered it obeys the order.
     *
     * @return
     */
    public String getIDChain() {
        if (getTransitions() == null) {
            return (isTop()) ? "T" : "";
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<Transition> iterator = getTransitions().iterator(); iterator.hasNext();) {
            sb.append(iterator.next().getId()).append("-");
        }
        return sb.toString();
    }

    public String getIDChainByFirstSorting(boolean withoutColor) {
        if (getTransitions() == null) {
            return (isTop()) ? "T" : "";
        }
        List<Transition> trans = new ArrayList<>(getTransitions());
        Comparator<Transition> comp = withoutColor ? new LexiLLTransitionIDWithoutColorComparator() : new LexiTransitionIDComparator();
        Collections.sort(trans, comp);
        StringBuilder sb = new StringBuilder();
        for (Iterator<Transition> iterator = trans.iterator(); iterator.hasNext();) {
            sb.append(iterator.next().getId()).append("-");
        }
        return sb.toString();
    }

}
