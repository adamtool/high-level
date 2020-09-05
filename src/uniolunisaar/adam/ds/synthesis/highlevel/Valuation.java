package uniolunisaar.adam.ds.synthesis.highlevel;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;

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

    public Valuation(Valuation v) {
        super(v.size());
        for (Entry<Variable, Color> entry : v.entrySet()) {
            Variable key = entry.getKey();
            Color value = entry.getValue();
            put(new Variable(key), new Color(value));
        }
    }

    public Valuation() {
    }

    public TreeMap<Variable, Color> getSorted() {
        TreeMap<Variable, Color> sorted = new TreeMap<>(new Comparator<Variable>() {
            @Override
            public int compare(Variable arg0, Variable arg1) {
                return arg0.getName().compareTo(arg1.getName());
            }
        });
        sorted.putAll(this);
        return sorted;
    }
}
