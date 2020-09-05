package uniolunisaar.adam.ds.synthesis.highlevel;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Manuel Gieseking
 */
public class ColorTokens extends HashSet<ColorToken> {

    private static final long serialVersionUID = 1L;

    public ColorTokens() {
    }

    public ColorTokens(Collection<? extends ColorToken> c) {
        super(c);
    }

    public ColorTokens(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ColorTokens(int initialCapacity) {
        super(initialCapacity);
    }

    public String toDotString() {
        StringBuilder sb = new StringBuilder();
        if (this.size() > 1) {
            sb.append("{");
        }
        int i = 1;
        for (ColorToken col : this) {
            sb.append(col.toString()).append(",");
            if (i++ % 3 == 0 && i < col.size() - 1) {
                sb.append("\\n");
            }
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
