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

}
