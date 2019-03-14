package uniolunisaar.adam.ds.highlevel.symmetries;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import uniolunisaar.adam.ds.highlevel.Color;

/**
 *
 * @author Manuel Gieseking
 */
public class RotationIterator implements Iterator<List<Color>> {

    private final List<Color> colors;
    private int idx;

    public RotationIterator(List<Color> colors) {
        this.colors = colors;
        this.idx = 0;
    }

    @Override
    public boolean hasNext() {
        return idx < colors.size();
    }

    @Override
    public List<Color> next() {
        List<Color> output = new ArrayList<>();
        for (int i = 0; i < colors.size(); i++) {
            output.add(colors.get((idx + i) % colors.size()));
        }
        ++idx;
        return output;
    }

}
