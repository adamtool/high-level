package uniolunisaar.adam.ds.synthesis.highlevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Manuel Gieseking
 */
public class StaticColorClass {

    private final String id;
    private final List<Color> colors;

    StaticColorClass(StaticColorClass sclass) {
        this.id = sclass.id;
        this.colors = new ArrayList<>(sclass.colors.size());
        for (Color color : sclass.colors) {
            colors.add(new Color(color));
        }
    }
//
//    StaticColorClass(String id) {
//        this.id = id;
//        this.colors = new ArrayList<>();
//    }

    StaticColorClass(String id, List<Color> colors) {
        this.id = id;
        this.colors = colors;
    }

//    StaticColorClass(String id, Color... col) {
//        this.id = id;
//        this.colors = Arrays.asList(col);
//    }

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
