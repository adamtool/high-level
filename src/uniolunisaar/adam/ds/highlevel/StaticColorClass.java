package uniolunisaar.adam.ds.highlevel;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author Manuel Gieseking
 */
public class StaticColorClass {

    private final String id;
    private final List<Color> colors;
//
//    StaticColorClass(String id) {
//        this.id = id;
//        this.colors = new ArrayList<>();
//    }

    StaticColorClass(String id, List<Color> colors) {
        this.id = id;
        this.colors = colors;
    }

    public boolean contains(Color c) {
        return colors.contains(c);
    }

    public List<Color> getColors() {
        return Collections.unmodifiableList(colors);
    }

    public String getId() {
        return id;
    }

    public String toDot() {
        StringBuilder sb = new StringBuilder("S: ");
        sb.append(getId()).append("=").append(colors.toString()).append("\n");
        return sb.toString();
    }
}
