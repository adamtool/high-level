package uniolunisaar.adam.ds.highlevel;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Manuel Gieseking
 */
public class ColorToken extends HashSet<Color> {

    public ColorToken() {
    }

    public ColorToken(Collection<? extends Color> c) {
        super(c);
    }

    public ColorToken(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ColorToken(int initialCapacity) {
        super(initialCapacity);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (this.size() > 1) {
            sb.append("{");
        }
        for (Color col : this) {
            sb.append(col.toString()).append(",");
        }
        if (size() >= 1) {
            sb.delete(sb.length() - 1, sb.length());
        }
        if (this.size() > 1) {
            sb.append("}");
        }
        return sb.toString();
    }

}
