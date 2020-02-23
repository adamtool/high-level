package uniolunisaar.adam.ds.graph;

import java.util.Set;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 */
public interface IDecisionSet<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>> extends StateIdentifier {

    public boolean hasTop(Set<DC> dcs);

    public boolean hasTop();

//    public Set<? extends IDecisionSet<P, T, DC>> resolveTop();
    public Set<S> resolveTop();

//    public Set<? extends IDecisionSet<P, T, DC>> fire(T t);
    public Set<S> fire(T t);
//    /**
//     * Changing the decision set is nice for not creating so much copies but using
//     * objects in sets which hashcode can change are evil.
//     *
//     * @param sym
//     * @deprecated
//     */
//    @Deprecated
//    public void apply(Symmetry sym);

//    public IDecisionSet<P, T, DC> apply(Symmetry sym);
    public S apply(Symmetry sym);

    public Set<P> getMarking();

    public boolean isMcut();

    public boolean isBad();

    public String toDot();

    @Override
    public int getId();

}
