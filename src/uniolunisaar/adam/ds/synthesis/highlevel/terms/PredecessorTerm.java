package uniolunisaar.adam.ds.synthesis.highlevel.terms;

import java.util.HashSet;
import java.util.Set;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.synthesis.highlevel.predicate.IPredicateTerm;
import uniolunisaar.adam.exceptions.synthesis.highlevel.NoNeighbourForUnorderedColorClassException;
import uniolunisaar.adam.exceptions.synthesis.highlevel.NoSuchColorException;

/**
 *
 * @author Manuel Gieseking
 */
public class PredecessorTerm implements IPredicateTerm<Color>, IArcTerm<Color>, IArcTupleElement<Color> {

    private final Variable x;
    private final HLPetriGame game;

    public PredecessorTerm(Variable x, HLPetriGame game) {
        this.x = x;
        this.game = game;
    }

    public Variable getVariable() {
        return x;
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> vars = new HashSet<>();
        vars.add(x);
        return vars;
    }

    @Override
    public Color getValue(Valuation valuation) throws NoSuchColorException, NoNeighbourForUnorderedColorClassException {
        return game.getNeighbourValue(valuation.get(x), true);
    }

    @Override
    public String toSymbol() {
        return "⊖ " + x.getName();//	U+2296   &#8854;        
    }

    @Override
    public String toString() {
        return "PREV(" + x.getName() + ")";
    }

}
