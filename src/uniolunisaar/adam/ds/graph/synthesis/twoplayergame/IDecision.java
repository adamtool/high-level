package uniolunisaar.adam.ds.graph.synthesis.twoplayergame;

import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 */
public interface IDecision<P, T> {

    public boolean isEnvDecision();

    public P getPlace();

    public boolean isChoosen(T t);

    public boolean isTop();

//    /**
//     * Changing the decision is nice for not creating so much copies but using
//     * objects in sets which hashcode can change are evil.
//     *
//     * @param sym
//     * @deprecated
//     */
//    @Deprecated
//    public void apply(Symmetry sym);
    public IDecision<P, T> apply(Symmetry sym);

    public String toDot();

}
