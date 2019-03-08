package uniolunisaar.adam.ds.highlevel;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author Manuel Gieseking
 */
public class ColorToken extends ArrayList<Color> {

    public ColorToken(Color... color) {
        this.addAll(Arrays.asList(color));
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
