package uniolunisaar.adam.ds.highlevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * This class is used for one token in the Petri game. These token can be tuples
 * (c_1, ..., c_2) of colors c_i.
 *
 * @author Manuel Gieseking
 */
public class ColorToken extends ArrayList<Color> {

    public ColorToken(Color... color) {
        this.addAll(Arrays.asList(color));
    }

    public ColorToken(Collection<? extends Color> c) {
        super(c);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.size() > 1) {
            sb.append("(");
        }
        for (int i = 0; i < this.size() - 1; i++) {
            sb.append(this.get(i).toString()).append(",");
        }
        if (this.size() >= 1) {
            sb.append(this.get(this.size() - 1).toString());
            if (this.size() > 1) {
                sb.append(")");
            }
        }
        return sb.toString();
    }

}
