package uniolunisaar.adam.ds.graph.synthesis.twoplayergame;

/**
 * Currently not yet used.
 *
 * @author Manuel Gieseking
 * @param <P>
 * @param <T>
 * @param <DC>
 * @param <S>
 * @param <ID>
 */
public class GameGraphState<P, T, DC extends IDecision<P, T>, S extends IDecisionSet<P, T, DC, S>, ID extends StateIdentifier> {

    private final S dcs;
    private final ID id;

    public GameGraphState(S dcs, ID id) {
        this.dcs = dcs;
        this.id = id;
    }

    public S getDcs() {
        return dcs;
    }

    public ID getId() {
        return id;
    }

}
