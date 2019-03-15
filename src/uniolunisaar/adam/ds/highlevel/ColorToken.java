package uniolunisaar.adam.ds.highlevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;

/**
 * This class is used for one token in the Petri game. These token can be tuples
 * (c_1, ..., c_2) of colors c_i.
 *
 * @author Manuel Gieseking
 */
public class ColorToken {

    private List<Color> tuple;

    public ColorToken(ColorToken token) {
        tuple = new ArrayList<>(token.tuple.size());
        for (Color color : token.tuple) {
            tuple.add(new Color(color));
        }
    }

    public ColorToken(Color... color) {
        tuple = new ArrayList<>();
        tuple.addAll(Arrays.asList(color));
    }

    public ColorToken(Collection<? extends Color> c) {
        tuple = new ArrayList<>(c);
    }

    public void apply(Symmetry sym) {
        List<Color> newTuple = new ArrayList<>(tuple.size());
        for (int i = 0; i < tuple.size(); i++) {
            newTuple.add(sym.get(tuple.get(i)));
        }
        tuple = newTuple;
    }

    // DELEGATES
    public int size() {
        return tuple.size();
    }

    public boolean add(Color arg0) {
        return tuple.add(arg0);
    }

    public Color get(int arg0) {
        return tuple.get(arg0);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (tuple.size() > 1) {
            sb.append("(");
        }
        for (int i = 0; i < tuple.size() - 1; i++) {
            sb.append(tuple.get(i).toString()).append(",");
        }
        if (tuple.size() >= 1) {
            sb.append(tuple.get(tuple.size() - 1).toString());
            if (tuple.size() > 1) {
                sb.append(")");
            }
        }
        return sb.toString();
    }

}
