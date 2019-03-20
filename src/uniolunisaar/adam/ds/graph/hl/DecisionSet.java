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
//    /**
//     * Changing the decision set is nice for not creating so much copies but using
//     * objects in sets which hashcode can change are evil.
//     *
//     * @param sym
//     * @deprecated
//     */
//    @Deprecated
//    public void apply(Symmetry sym);

    public DecisionSet<P, T, DC> apply(Symmetry sym);

    public boolean isMcut();

    public boolean isBad();

    public String toDot();

    public int getId();
}
