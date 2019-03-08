package uniolunisaar.adam.ds.highlevel.terms;

import java.util.HashSet;
import java.util.Set;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicateTerm;
import uniolunisaar.adam.exceptions.highlevel.NoSuccessorForUnorderedColorClassException;
import uniolunisaar.adam.exceptions.highlevel.NoSuchColorException;

/**
 *
 * @author Manuel Gieseking
 */
public class SuccessorTerm implements IPredicateTerm<Color>, IArcTerm<Color>, IArcTupleElement<Color> {

    private final Variable x;
    private final HLPetriGame game;

// ONLY NEEDED IF WE WANT TO DEFINE SUCCESSORS ALSO FOR TUPLES!
//    public SuccessorTerm(Variable x, HLPetriGame game) throws DatastructureException {
//        // this is only valid if all basic color classes of the variable are 
//        // ordered
//        List<String> colorClass = x.getColorClass();
//        for (String colorClas : colorClass) {
//            if (game.isBasicColorClass(colorClas)) {
//                if (!game.getBasicColorClass(colorClas).isOrdered()) {
//                    throw new NoSuccessorForUnorderedColorClassException("The basic color class " + colorClas + " of variable " + x.getName() + " is not ordered. No successor proposition can be created.");
//                }
//            } else {
//                BasicColorClass cclass = game.getBasicColorClassOfStaticSubclass(colorClas);
//                if (cclass == null) {
//                    throw new NoSuchColorClassException("There is no basic color class or static subclass with the id " + colorClas + " in the Petri game " + game.getName());
//                }
//                if (!game.getBasicColorClassOfStaticSubclass(colorClas).isOrdered()) {
//                    throw new NoSuccessorForUnorderedColorClassException("The static color class " + colorClas + " of variable " + x.getName() + " is not ordered. No successor proposition can be created.");
//                }
//            }
//        }
//        this.x = x;
//        this.game = game;
//    }
    public SuccessorTerm(Variable x, HLPetriGame game) {
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
    public Color getValue(Valuation valuation) throws NoSuchColorException, NoSuccessorForUnorderedColorClassException {
        return game.getSuccessorValue(valuation.get(x));
    }

    @Override
    public String toSymbol() {
        return "⨁ " + x.getName();//	&#10753;	&#x2A01;
    }

    @Override
    public String toString() {
        return "SUCC(" + x.getName() + ")";
    }

}
