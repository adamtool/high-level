package uniolunisaar.adam.ds.synthesis.highlevel.predicate;

import java.util.HashSet;
import java.util.Set;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public enum Constants implements IPredicate {

    TRUE {

        @Override
        public Set<Variable> getVariables() {
            return new HashSet<>();
        }

        @Override
        public boolean check(Valuation valuation) {
            return true;
        }

        @Override
        public String toSymbol() {
            return ""; //return "⊤";// "\u22A4";
        }

        @Override
        public String toString() {
            return "";
        }
    },
    FALSE {

        @Override
        public Set<Variable> getVariables() {
            return new HashSet<>();
        }

        @Override
        public boolean check(Valuation valuation) {
            return false;
        }

        @Override
        public String toSymbol() {
//            return "⊥";// "\u22A5";
            return "FALSE";
        }

        @Override
        public String toString() {
            return "FALSE";
        }
    }
}
