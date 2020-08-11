package uniolunisaar.adam.ds.highlevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uniolunisaar.adam.exceptions.highlevel.NoNeighbourForUnorderedColorClassException;

/**
 *
 * @author Manuel Gieseking
 */
public class BasicColorClass {

    private final String id;
    private boolean ordered;
    private final List<Color> colors;
    private final Map<String, StaticColorClass> staticSubclasses;
//
//    BasicColorClass(String id) {
//        this.id = id;
//        this.ordered = false;
//        this.colors = new ArrayList<>();
//        this.staticSubclasses = new HashMap<>();
//    }
//

    BasicColorClass(BasicColorClass cclass) {
        this.id = cclass.id;
        this.ordered = cclass.ordered;
        this.colors = new ArrayList<>(cclass.colors.size());
        for (Color color : cclass.colors) {
            colors.add(new Color(color));
        }
        this.staticSubclasses = new HashMap<>();
        for (Map.Entry<String, StaticColorClass> entry : staticSubclasses.entrySet()) {
            String key = entry.getKey();
            StaticColorClass value = entry.getValue();
            this.staticSubclasses.put(key, new StaticColorClass(value));
        }
    }

    BasicColorClass(String id, boolean ordered) {
        this.id = id;
        this.ordered = ordered;
        this.colors = new ArrayList<>();
        this.staticSubclasses = new HashMap<>();
    }

    BasicColorClass(String id, boolean ordered, List<Color> colors) {
        this.id = id;
        this.ordered = ordered;
        this.colors = colors;
        this.staticSubclasses = new HashMap<>();
    }

    void addColors(List<Color> colors) {
        this.colors.addAll(colors);
    }

    void addStaticColorClass(String id, StaticColorClass clazz) {
        this.staticSubclasses.put(id, clazz);
    }

    /**
     * Checks whether this basic color class is valid, i.e., whether the static
     * subclasses really partition the basic class.
     *
     * @return true iff the class is valid.
     */
    public boolean checkValid() {
        List<Color> union = new ArrayList<>();
        for (StaticColorClass cClass : staticSubclasses.values()) {
            union.addAll(cClass.getColors());
            List<Color> intersection = new ArrayList<>(cClass.getColors());
            intersection.retainAll(union);
            if (!intersection.isEmpty()) {
                return false;
            }
        }
        boolean allIncluded = union.containsAll(colors) && union.size() == colors.size();
        return allIncluded;
    }

    public int nbStaticSubclasses() {
        return staticSubclasses.size();
    }

    public boolean hasStaticSubclasses() {
        return !staticSubclasses.isEmpty();
    }

    public boolean hasStaticSubclass(String id) {
        return staticSubclasses.containsKey(id);
    }

    public List<List<Color>> getStaticSubclassesColors() {
        List<List<Color>> statColors = new ArrayList<>();
        for (StaticColorClass value : staticSubclasses.values()) {
            statColors.add(value.getColors());
        }
        return statColors;
    }

    /**
     * Returns the corresponding static subclass if existent, otherwise null.
     *
     * @param c
     * @return
     */
    public String getStaticSubclassID(Color c) {
        for (Map.Entry<String, StaticColorClass> subClass : staticSubclasses.entrySet()) {
            String id = subClass.getKey();
            StaticColorClass clazz = subClass.getValue();
            if (clazz.contains(c)) {
                return id;
            }
        }
        return null;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public String getId() {
        return id;
    }

    public Map<String, StaticColorClass> getStaticSubclasses() {
        return Collections.unmodifiableMap(this.staticSubclasses);
    }

    /**
     * Returns the predecessor/successor of c iff this basic color class
     * contains c.Throws NoSuccessorForUnorderedColorClassException iff this
     * class contains c and it is not ordered.
     *
     *
     * @param c - color
     * @param pre - if true predecessor else successor is returned
     * @return null iff this class does not contain c
     */
    public Color getNeighbourValue(Color c, boolean pre) throws NoNeighbourForUnorderedColorClassException {
        for (int i = 0; i < colors.size(); i++) {
            if (colors.get(i).equals(c)) {
                if (!this.isOrdered()) {
                    throw new NoNeighbourForUnorderedColorClassException("The basic color class " + id + " of color " + c.getId() + " is not ordered. No predecessor/successor is defined.");
                }
                return colors.get(((pre) ? (i - 1) : (i + 1)) % colors.size());
            }
        }
        return null;
    }

    public List<Color> getColors() {
        return Collections.unmodifiableList(colors);
    }

    // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%% DELEGATES
    public boolean containsColor(Color c) {
        return colors.contains(c);
    }

    public String toDot() {
        StringBuilder sb = new StringBuilder(ordered ? "O: " : "U: ");
        sb.append(getId()).append("=").append(colors.toString());
        if (!staticSubclasses.isEmpty()) {
            sb.append("\n");
        }
        for (StaticColorClass subclass : staticSubclasses.values()) {
            sb.append(subclass.toDot()).append("\n");
        }
        return sb.toString();
    }
}
