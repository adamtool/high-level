package uniolunisaar.adam.ds.highlevel;

import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author Manuel Gieseking
 */
public class ColorDomain extends ArrayList<String> {

    public static final long serialVersionUID = 0x1l;

    ColorDomain(int initialCapacity) {
        super(initialCapacity);
    }

    ColorDomain() {
    }

    ColorDomain(Collection<? extends String> c) {
        super(c);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.size() - 1; i++) {
            sb.append(this.get(i)).append(" x ");
        }
        if (this.size() >= 1) {
            sb.append(this.get(this.size() - 1));
        }
        return sb.toString();
    }

}
