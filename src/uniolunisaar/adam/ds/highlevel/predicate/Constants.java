package uniolunisaar.adam.ds.highlevel.predicate;

import uniolunisaar.adam.ds.highlevel.Valuation;

/**
 *
 * @author Manuel Gieseking
 */
public enum Constants implements IPredicate {

    TRUE {
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
