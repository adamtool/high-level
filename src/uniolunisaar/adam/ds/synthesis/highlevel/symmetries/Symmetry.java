package uniolunisaar.adam.ds.synthesis.highlevel.symmetries;

import java.util.HashMap;
import java.util.Map;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;

/**
 *
 * @author Manuel Gieseking
 */
public class Symmetry extends HashMap<Color, Color> {

    private static final long serialVersionUID = 1L;

    public Symmetry() {
    }

    public Symmetry(Map<? extends Color, ? extends Color> m) {
        super(m);
    }

}
