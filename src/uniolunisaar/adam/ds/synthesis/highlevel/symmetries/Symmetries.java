package uniolunisaar.adam.ds.synthesis.highlevel.symmetries;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import uniolunisaar.adam.ds.synthesis.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;

/**
 *
 * @author Manuel Gieseking
 */
public class Symmetries implements Iterable<Symmetry> {

    private final List<List<Color>> unordered;
    private final List<List<Color>> ordered;
    private final List<List<Color>> id;

    public Symmetries(Collection<BasicColorClass> classes) {
        unordered = new ArrayList<>();
        ordered = new ArrayList<>();
        id = new ArrayList<>();
        for (BasicColorClass classe : classes) {
            if (!classe.isOrdered()) {
                if (classe.hasStaticSubclasses()) {
                    unordered.addAll(classe.getStaticSubclassesColors());
                } else {
                    unordered.add(classe.getColors());
                }
            } else {
                if (classe.nbStaticSubclasses() > 1) {
                    id.add(classe.getColors());
                } else {
                    ordered.add(classe.getColors());
                }
            }
        }
    }

    @Override
    public SymmetryIterator iterator() {
        return new SymmetryIterator(unordered, ordered, id);
    }

}
