package uniolunisaar.adam.ds.synthesis.highlevel;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class ValuationIterator implements Iterator<Valuation> {

    private final Map<Variable, Integer> idxs;
    private final Map<Variable, List<Color>> var2CClass;
    private final Variable[] vars;
    private final Valuation val; // saves the current valuation

    public ValuationIterator(Map<Variable, List<Color>> var2CClass) {
        this.var2CClass = var2CClass;
        this.idxs = new HashMap<>();
        vars = new Variable[this.var2CClass.size()];
        // init the indizes to all 0 but the last one to -1 for starting
        int i = 0;
        for (Variable variable : this.var2CClass.keySet()) {
            idxs.put(variable, 0);
            vars[i++] = variable;
        }
        idxs.put(vars[i - 1], -1);
        this.val = new Valuation();
        for (int j = 0; j < vars.length; j++) {
            val.put(vars[j], var2CClass.get(vars[j]).get(0));
        }
    }

    private void setCurrentValuation() {
        for (Variable variable : vars) {
            val.put(variable, var2CClass.get(variable).get(idxs.get(variable)));
        }
    }

    @Override
    public boolean hasNext() {
        for (Variable variable : vars) {
            if (idxs.get(variable) < var2CClass.get(variable).size() - 1) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Valuation next() {
        // calculate the new valuation
        // find the first list still having a successor from the back
        int pos = -1;
        for (int i = vars.length - 1; i >= 0; i--) {
            Variable var = vars[i];
            if (idxs.get(var) < var2CClass.get(var).size() - 1) {
                pos = i;
                break;
            }
        }
        if (pos == -1) {
            throw new NoSuchElementException();
        }
        // from this index set all other lists to the first value
        for (int i = pos + 1; i < vars.length; i++) {
            idxs.put(vars[i], 0);
        }
        // for the list itself do one step
        idxs.put(vars[pos], idxs.get(vars[pos]) + 1);
        // set this new valuation
        setCurrentValuation();
        return new Valuation(val); // return the new valuation
    }

}
