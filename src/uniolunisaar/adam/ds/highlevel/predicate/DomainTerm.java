package uniolunisaar.adam.ds.highlevel.predicate;

import java.util.HashSet;
import java.util.Set;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassType;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.exceptions.highlevel.NoSuchColorClassException;
import uniolunisaar.adam.exceptions.highlevel.NoSuchColorException;

/**
 *
 * @author Manuel Gieseking
 */
public class DomainTerm implements IPredicateTerm<ColorClassType> {

    private final Variable x;
    private final HLPetriGame game;

    public DomainTerm(Variable x, HLPetriGame game) {
        this.x = x;
        this.game = game;
    }

    @Override
    public Set<Variable> getVariables() {
        Set<Variable> vars = new HashSet<>();
        vars.add(x);
        return vars;
    }

    @Override
    public ColorClassType getValue(Valuation valuation) throws NoSuchColorClassException, NoSuchColorException {
        Color c = valuation.get(x);
        BasicColorClass bcc = game.getBasicColorClass(c);
        if (bcc == null) {
            throw new NoSuchColorException("The color " + c.getId() + " of the valuation " + valuation + " does not exists in the Petri game '" + game.getName() + "'.");
        }
        String id = bcc.getStaticSubclassID(c);
        if (id == null) {
            throw new NoSuchColorClassException("The color  " + c.getId() + " of the valuation " + valuation + " is not in any static subclass of the Petri game '" + game.getName() + "'.");
        }
        return new ColorClassType(id);
    }

    @Override
    public String toSymbol() {
        return "𝒟(" + x.getName() + ")";
    }

    @Override
    public String toString() {
        return "D(" + x.getName() + ")";
    }

}
