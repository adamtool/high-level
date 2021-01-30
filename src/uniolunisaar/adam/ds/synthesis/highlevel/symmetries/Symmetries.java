package uniolunisaar.adam.ds.synthesis.highlevel.symmetries;

import java.util.ArrayList;
import java.util.Collection;
import uniolunisaar.adam.ds.synthesis.highlevel.BasicColorClass;

/**
 *
 * @author thewn
 */
public class Symmetries extends ArrayList<Symmetry> {

    public Symmetries(Collection<BasicColorClass> classes) {
        SymmetriesWithoutStoring syms = new SymmetriesWithoutStoring(classes);
//        Symmetries syms = new Symmetries(classes);
        for (SymmetryIterator iterator = syms.iterator(); iterator.hasNext();) {
            Symmetry next = iterator.next();
            add(next);
        }
    }
}
