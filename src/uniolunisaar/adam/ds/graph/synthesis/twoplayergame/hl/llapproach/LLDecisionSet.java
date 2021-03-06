package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach;

import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.CommitmentSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.DecisionSet;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.EnvDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.ILLDecision;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.explicit.SysDecision;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;

/**
 *
 * @author Manuel Gieseking
 */
public class LLDecisionSet extends DecisionSet {

    public LLDecisionSet(LLDecisionSet dcs) {
        super(dcs);
    }

    /**
     * ATTENTION: don't change the elements of the set afterwards, otherwise
     * contains won't work anymore
     *
     * @param decisions
     * @param mcut
     * @param bad
     * @param game
     */
    public LLDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {
        super(decisions, mcut, bad, game);
    }

    @Override
    public CommitmentSet createCommitmentSet(CommitmentSet c) {
        return new LLCommitmentSet(c);
    }

    @Override
    public CommitmentSet createCommitmentSet(PetriGameWithTransits game, boolean isTop) {
        return new LLCommitmentSet(game, isTop);
    }

    @Override
    public CommitmentSet createCommitmentSet(PetriGameWithTransits game, Set<Transition> transitions) {
        return new LLCommitmentSet(game, transitions);
    }

    @Override
    public SysDecision createSysDecision(PetriGameWithTransits game, Place place, CommitmentSet c) {
        return new LLSysDecision(game, place, c);
    }

    @Override
    public SysDecision createSysDecision(SysDecision decision) {
        return new LLSysDecision(decision);
    }

    @Override
    public EnvDecision createEnvDecision(PetriGameWithTransits game, Place place) {
        return new LLEnvDecision(game, place);
    }

    @Override
    public EnvDecision createEnvDecision(EnvDecision decision) {
        return new LLEnvDecision(decision);
    }

    @Override
    public DecisionSet createDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGameWithTransits game) {
        return new LLDecisionSet(decisions, mcut, bad, game);
    }

}
