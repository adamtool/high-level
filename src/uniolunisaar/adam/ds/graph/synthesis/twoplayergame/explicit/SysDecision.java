package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit;

import java.util.Iterator;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.AbstractSysDecision;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;

/**
 *
 * @author Manuel Gieseking
 */
public class SysDecision extends AbstractSysDecision<Place, Transition, CommitmentSet> implements ILLDecision {

    private final PetriGameWithTransits game;

    /**
     * Copy-Constructor
     *
     * @param dcs
     */
    public SysDecision(SysDecision dcs) {
        super(dcs.getPlace(), new CommitmentSet(dcs.getC()));
        this.game = dcs.game;
    }

    public SysDecision(PetriGameWithTransits game, Place place, CommitmentSet c) {
        super(place, c);
        this.game = game;
    }

    @Override
    public SysDecision apply(Symmetry sym) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.    
    }

    @Override
    public String toDot() {
        StringBuilder sb = new StringBuilder("(");
        sb.append(getPlace().getId()).append(", ");
        sb.append(getC().toDot());
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toString() {
        return toDot();
    }

    protected PetriGameWithTransits getGame() {
        return game;
    }

    @Override
    public String getIDChain() {
        return getPlace().getId().concat("+").concat(getC().getIDChain());
    }

    private String idByFirstSorting = null;

    @Override
    public String getIDChainByFirstSorting() {
        if (idByFirstSorting == null) {
            idByFirstSorting = getPlace().getId().concat("+").concat(getC().getIDChainByFirstSorting(false));
        }
        return idByFirstSorting;
    }

    private String commitIdByFirstSorting = null;

    public String getCommitmentSetIDChainByFirstSorting(boolean withoutColor) {
        if (commitIdByFirstSorting == null) {
            commitIdByFirstSorting = getC().getIDChainByFirstSorting(withoutColor);
        }
        return commitIdByFirstSorting;
    }

    public int getCommitmentSetSize() {
        return getC().size();
    }

    public Iterator<Transition> getCommitmentSetIterator() {
        return getC().getIterator();
    }
}
