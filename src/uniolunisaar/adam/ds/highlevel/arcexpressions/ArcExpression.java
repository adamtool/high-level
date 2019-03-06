package uniolunisaar.adam.ds.highlevel.arcexpressions;

import java.util.ArrayList;

/**
 *
 * @author Manuel Gieseking
 */
public class ArcExpression extends ArrayList<IArcTerm> {

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.size() - 1; i++) {
            sb.append(this.get(i).toString()).append("+");
        }
        if (this.size() >= 1) {
            sb.append(this.get(this.size() - 1).toString());
        }
        return sb.toString();
    }

}
