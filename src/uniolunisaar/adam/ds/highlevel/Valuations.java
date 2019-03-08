package uniolunisaar.adam.ds.highlevel;

import java.util.List;
import java.util.Map;
import uniolunisaar.adam.ds.highlevel.terms.Variable;

/**
 *
 * @author Manuel Gieseking
 */
public class Valuations implements Iterable<Valuation> {

    private final Map<Variable, List<Color>> var2CClass;

    public Valuations(Map<Variable, List<Color>> var2CClass) {
        this.var2CClass = var2CClass;
    }

    @Override
    public ValuationIterator iterator() {
        return new ValuationIterator(var2CClass);
    }

}
