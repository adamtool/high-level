package uniolunisaar.adam.ds.highlevel.predicate;

/**
 *
 * @author Manuel Gieseking
 */
public class Constants {

    public static class TRUE implements IPredicate {

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
    }

    public static class FALSE implements IPredicate {

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
