package uniolunisaar.adam.ds.graph.hl;

import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

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

    public void apply(Symmetry sym);

    public String toDot();

}
