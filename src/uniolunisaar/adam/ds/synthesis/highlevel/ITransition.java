package uniolunisaar.adam.ds.synthesis.highlevel;

import java.util.Set;

/**
 *
 * @author Manuel Gieseking
 * @param <P>
 */
public interface ITransition<P extends IPlace<? extends ITransition<P>>> {

    public Set<P> getPreset();

    public Set<P> getPostset();
}
