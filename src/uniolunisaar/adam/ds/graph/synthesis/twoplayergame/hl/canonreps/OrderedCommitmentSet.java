package uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.canonreps;

import java.util.Map;
import java.util.TreeSet;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.graph.synthesis.twoplayergame.hl.llapproach.LLCommitmentSet;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 *
 * @author Manuel Gieseking
 */
public class OrderedCommitmentSet extends LLCommitmentSet {

    public OrderedCommitmentSet(PetriGameWithTransits game, boolean isTop) {
        super(game, isTop);
    }

    public OrderedCommitmentSet(OrderedCommitmentSet c) {
        super(c.getGame(), c.getTransitions());
    }

//    public OrderedCommitmentSet(PetriGameWithTransits game, Transition... transitions) {
//        super(game, false);
//        Set<Transition> tr = new TreeSet<>(new LexiTransitionIDComparator());
//        tr.addAll(Arrays.asList(transitions));
//        setTransitions(tr);
//    }
//
    public OrderedCommitmentSet(PetriGameWithTransits game, TreeSet<Transition> transitions) {
        super(game, transitions);
    }

    /**
     * It is the same semantics as in the super class. Here we only create a
     * OrderedCommitmentSet and use TreeSets as sets.
     *
     * @param sym
     * @return
     */
    @Override
    public OrderedCommitmentSet apply(Symmetry sym) {
        if (isTop()) {
            return new OrderedCommitmentSet(super.getGame(), true);
        }
        TreeSet<Transition> tr = new TreeSet<>(new LexiTransitionIDComparator());
        for (Transition transition : getTransitions()) {
            String hlID = HL2PGConverter.getOrigID(transition);
            Valuation val = HL2PGConverter.getValuation(transition);
            Valuation newVal = new Valuation();
            for (Map.Entry<Variable, Color> entry : val.entrySet()) {
                Variable var = entry.getKey();
                Color c = entry.getValue();
                newVal.put(var, sym.get(c));
            }
            tr.add(getGame().getTransition(HL2PGConverter.getTransitionID(hlID, newVal)));
        }
        return new OrderedCommitmentSet(getGame(), tr);
    }

}
