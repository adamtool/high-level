package uniolunisaar.adam.ds.graph.hl.llapproach;

import uniolunisaar.adam.ds.graph.explicit.*;
import java.util.Set;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.petrigame.PetriGame;

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
    public LLDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGame game) {
        super(decisions, mcut, bad, game);
    }

    @Override
    public CommitmentSet createCommitmentSet(CommitmentSet c) {
        return new LLCommitmentSet(c);
    }

    @Override
    public CommitmentSet createCommitmentSet(PetriGame game, boolean isTop) {
        return new LLCommitmentSet(game, isTop);
    }

    @Override
    public CommitmentSet createCommitmentSet(PetriGame game, Set<Transition> transitions) {
        return new LLCommitmentSet(game, transitions);
    }

    @Override
    public SysDecision createSysDecision(PetriGame game, Place place, CommitmentSet c) {
        return new LLSysDecision(game, place, c);
    }

    @Override
    public SysDecision createSysDecision(SysDecision decision) {
        return new LLSysDecision(decision);
    }

    @Override
    public EnvDecision createEnvDecision(PetriGame game, Place place) {
        return new LLEnvDecision(game, place);
    }

    @Override
    public EnvDecision createEnvDecision(EnvDecision decision) {
        return new LLEnvDecision(decision);
    }

    @Override
    public DecisionSet createDecisionSet(Set<ILLDecision> decisions, boolean mcut, boolean bad, PetriGame game) {
        return new LLDecisionSet(decisions, mcut, bad, game);
    }

}
