package uniolunisaar.adam.logic.hl;

import java.util.Set;
import java.util.Stack;
import uniolunisaar.adam.ds.graph.hl.AbstractSymbolicGameGraph;
import uniolunisaar.adam.ds.graph.hl.DecisionSet;
import uniolunisaar.adam.ds.graph.hl.IDecision;
import uniolunisaar.adam.ds.graph.hl.SRGFlow;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.highlevel.symmetries.SymmetryIterator;

/**
 *
 * @author Manuel Gieseking
 */
public class SGGBuilder {


    /**
     * Adds a successor only if there is not already any equivalence class
     * (regarding the symmetries) containing the successor. The corresponding
     * flows are added anyways.
     *
     * @param succs
     * @param syms
     * @param todo
     * @param srg
     */
    static <P, T, DC extends IDecision<P, T>, S extends DecisionSet<P, T, DC>, F extends SRGFlow<T>>
            void addSuccessors(S pre, T t, Set<S> succs, Symmetries syms, Stack<Integer> todo, AbstractSymbolicGameGraph<P, T, DC, S, SRGFlow<T>> srg) {
        for (S succ : succs) {
            boolean newOne = true;
            int id = succ.getId();
            S copySucc = succ;
            for (SymmetryIterator iti = syms.iterator(); iti.hasNext();) {
                Symmetry sym = iti.next(); // todo: get rid of the identity symmetry, just do it in this case before looping
                copySucc = (S) succ.apply(sym);
                if (srg.contains(copySucc)) {
                    newOne = false;
                    break;
                }
            }

            if (newOne) {
                srg.addState(succ);
                todo.add(id);
            } else {
                id = copySucc.getId();
            }
            srg.addFlow(new SRGFlow(pre.getId(), t, id));
        }
    }
}
