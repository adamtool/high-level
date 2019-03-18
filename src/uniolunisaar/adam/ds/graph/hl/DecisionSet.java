package uniolunisaar.adam.ds.graph.hl;

import java.util.Set;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 * @param <DC>
 */
public interface DecisionSet<P, T, DC extends IDecision<P, T>> {

    public boolean hasTop(Set<DC> dcs);

    public boolean hasTop();

    public Set<? extends DecisionSet> resolveTop();

    public Set<? extends DecisionSet> fire(T t);

    public void apply(Symmetry sym);
}
