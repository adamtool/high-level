package uniolunisaar.adam.ds.highlevel.symmetries;

import uniolunisaar.adam.ds.highlevel.*;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.apache.commons.collections4.iterators.PermutationIterator;

/**
 *
 * @author Manuel Gieseking
 */
public class SymmetryIterator implements Iterator<Symmetry> {

    private final List<List<Color>> unordered;
    private final List<List<Color>> ordered;
    private final List<List<Color>> id;

    private final Iterator<List<Color>>[] iterators;

    private final Symmetry sym; // saves the current symmetry

    public SymmetryIterator(List<List<Color>> unordered, List<List<Color>> ordered, List<List<Color>> id) {
        this.unordered = unordered;
        this.ordered = ordered;
        this.id = id;

        // Initialize iterators
        iterators = new Iterator[unordered.size() + ordered.size()];
        int i = 0;
        for (List<Color> colors : unordered) {
            iterators[i++] = new PermutationIterator<>(colors);
        }
        for (List<Color> colors : ordered) {
            iterators[i++] = new RotationIterator(colors);
        }

        // initialize the current symmetry  (put the first element for every put the last iterator (this is done by calling next)
        sym = new Symmetry();
        for (int j = 0; j < iterators.length - 1; j++) {
            populateSymmetry(j);
        }
        // add the identity
        for (List<Color> colors : id) {
            for (Color color : colors) {
                sym.put(color, color);
            }
        }
    }

    private void populateSymmetry(int i) {
        List<Color> colors = iterators[i].next();
        for (int k = 0; k < colors.size(); k++) {
            Color c = i < unordered.size() ? unordered.get(i).get(k) : ordered.get(i - unordered.size()).get(k);
            sym.put(c, colors.get(k));
        }
    }

    @Override
    public boolean hasNext() {
        for (Iterator iterator : iterators) {
            if (iterator.hasNext()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Symmetry next() {
        // calculate the new symmetry
        // find the first iterator still having a successor from the back
        int pos = -1;
        for (int i = iterators.length - 1; i >= 0; i--) {
            if (iterators[i].hasNext()) {
                pos = i;
                break;
            }
        }
        if (pos == -1) {
            throw new NoSuchElementException();
        }
        // from this index set all other iterators to the first value
        for (int i = pos + 1; i < iterators.length; i++) {
            iterators[i] = (i < unordered.size()) ? new PermutationIterator(unordered.get(i)) : new RotationIterator(ordered.get(i - unordered.size()));
        }

        // populate the symmetry with the new values for this iterator and all future ones
        for (int i = pos; i < iterators.length; i++) {
            populateSymmetry(i);
        }

        return sym;
    }

}
