package uniolunisaar.adam.ds.graph.hl;

import java.util.Set;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 */
public interface IDecisionSet<P, T, DC extends IDecision<P, T>> extends StateIdentifier {

    public boolean hasTop(Set<DC> dcs);

    public boolean hasTop();

    public Set<? extends IDecisionSet<P, T, DC>> resolveTop();

    public Set<? extends IDecisionSet<P, T, DC>> fire(T t);
//    /**
//     * Changing the decision set is nice for not creating so much copies but using
//     * objects in sets which hashcode can change are evil.
//     *
//     * @param sym
//     * @deprecated
//     */
//    @Deprecated
//    public void apply(Symmetry sym);

    public IDecisionSet<P, T, DC> apply(Symmetry sym);

    public boolean isMcut();

    public boolean isBad();

    public String toDot();

    public int getId();

}
