package uniolunisaar.adam.ds.highlevel;

import java.util.HashMap;
import java.util.Map;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class Valuation extends HashMap<Variable, Color> {

    public static final long serialVersionUID = 0x1l;

    public Valuation(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public Valuation(int initialCapacity) {
        super(initialCapacity);
    }

    public Valuation(Map<? extends Variable, ? extends Color> m) {
        super(m);
    }

    public Valuation() {
    }

}
