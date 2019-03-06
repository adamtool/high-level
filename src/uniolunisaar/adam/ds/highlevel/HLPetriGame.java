package uniolunisaar.adam.ds.highlevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.predicate.Constants;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.exceptions.highlevel.IdentifierAlreadyExistentException;
import uniolunisaar.adam.exceptions.highlevel.NoSuccessorForUnorderedColorClassException;
import uniolunisaar.adam.exceptions.highlevel.NoSuchColorDomainException;
import uniolunisaar.adam.exceptions.highlevel.NoSuchColorException;

/**
 *
 * @author Manuel Gieseking
 */
public class HLPetriGame {

    private final Map<String, BasicColorClass> colorClasses;
    private final PetriGame game;

    public HLPetriGame(String name) {
        game = new PetriGame(name);
        colorClasses = new HashMap<>();
    }

    public void createBasicColorClass(String id, boolean ordered, String... colors) throws IdentifierAlreadyExistentException {
        if (colorClasses.containsKey(id)) {
            throw new IdentifierAlreadyExistentException("The basic color identifier " + id + " already exists in the Petri game '" + game.getName() + "'.");
        }
        List<Color> cls = new ArrayList<>();
        for (int i = 0; i < colors.length; i++) {
            cls.add(new Color(colors[i]));
        }
        colorClasses.put(id, new BasicColorClass(id, ordered, cls));
    }

    public void createBasicColorClass(String id, boolean ordered, Pair<String, String[]>... staticSubClasses) throws IdentifierAlreadyExistentException {
        if (colorClasses.containsKey(id)) {
            throw new IdentifierAlreadyExistentException("The basic color identifier " + id + " already exists in the Petri game '" + game.getName() + "'.");
        }
        BasicColorClass bcc = new BasicColorClass(id, ordered);
        colorClasses.put(id, bcc);
        for (Pair<String, String[]> staticSubClass : staticSubClasses) {
            String subClassID = staticSubClass.getFirst();
            if (hasStaticSubclass(subClassID)) {
                throw new IdentifierAlreadyExistentException("The static subclass identifier " + subClassID + " already exists in the Petri game '" + game.getName() + "'.");
            }
            List<Color> cls = new ArrayList<>();
            String[] colors = staticSubClass.getSecond();
            for (int i = 0; i < colors.length; i++) {
                cls.add(new Color(colors[i]));
            }
            bcc.addColors(cls);
            bcc.addStaticColorClass(subClassID, new StaticColorClass(subClassID, cls));
        }
    }

// %%%%%%%%%%%%%%%%%% PLACES    
    public Place createSysPlace(String... colorClasses) throws NoSuchColorDomainException {
        Place p = game.createPlace();
        setColorDomain(p, new ColorDomain(Arrays.asList(colorClasses)));
        return p;
    }

    public Place createSysPlace(String placeID, String... colorClasses) throws NoSuchColorDomainException {
        Place p = game.createPlace(placeID);
        setColorDomain(p, new ColorDomain(Arrays.asList(colorClasses)));
        return p;
    }

    public Place createEnvPlace(String... colorClasses) throws NoSuchColorDomainException {
        Place p = game.createEnvPlace();
        setColorDomain(p, new ColorDomain(Arrays.asList(colorClasses)));
        return p;
    }

    public Place createEnvPlace(String placeID, String... colorClasses) throws NoSuchColorDomainException {
        Place p = game.createEnvPlace(placeID);
        setColorDomain(p, new ColorDomain(Arrays.asList(colorClasses)));
        return p;
    }

    private void setColorDomain(Place p, ColorDomain domain) throws NoSuchColorDomainException {
        for (int i = 0; i < domain.size(); i++) {
            if (!colorClasses.containsKey(domain.get(i))) {
                throw new NoSuchColorDomainException("The color domain " + domain.get(i) + " does not exists in the Petri net " + getName());
            }
        }
        HLPetriGameExtensionHandler.setColorClasses(p, domain);
    }

    public ColorDomain getColorDomain(Place p) {
        return HLPetriGameExtensionHandler.getColorDomain(p);
    }

    public BasicColorClass[] getBasicColorClasses(Place p) {
        ColorDomain classes = HLPetriGameExtensionHandler.getColorDomain(p);
        BasicColorClass[] c = new BasicColorClass[classes.size()];
        for (int i = 0; i < c.length; i++) {
            c[i] = colorClasses.get(classes.get(i));
        }
        return c;
    }

// %%%%%%%%%%%%%%% TRANSITIONS
    public Transition createTransition() {
        Transition t = game.createTransition();
        HLPetriGameExtensionHandler.setPredicate(t, Constants.TRUE);
        return t;
    }

    public Transition createTransition(IPredicate pred) {
        Transition t = game.createTransition();
        HLPetriGameExtensionHandler.setPredicate(t, pred);
        return t;
    }

    public Transition createTransition(String id) {
        Transition t = game.createTransition(id);
        HLPetriGameExtensionHandler.setPredicate(t, Constants.TRUE);
        return t;
    }

    public Transition createTransition(String id, IPredicate pred) {
        Transition t = game.createTransition(id);
        HLPetriGameExtensionHandler.setPredicate(t, pred);
        return t;
    }

    public IPredicate getPredicate(Transition t) {
        return HLPetriGameExtensionHandler.getPredicate(t);
    }

// %%%%%%%%%%%%%%%%%%%% FLOWS   
    public Flow createFlow(String sourceId, String targetId) {
        return game.createFlow(sourceId, targetId);
    }

    public Flow createFlow(String sourceId, String targetId, int weight) {
        return game.createFlow(sourceId, targetId, weight);
    }

    public Flow createFlow(Node source, Node target, int weight) {
        return game.createFlow(source, target, weight);
    }

    public Flow createFlow(Node source, Node target) {
        return game.createFlow(source, target);
    }

    public boolean isBasicColorClass(String id) {
        return colorClasses.containsKey(id);
    }

    public BasicColorClass getBasicColorClass(String id) {
        return colorClasses.get(id);
    }

    /**
     *
     * @param color
     * @return the corresponding color class or null
     */
    public BasicColorClass getBasicColorClass(Color color) {
        for (BasicColorClass cc : colorClasses.values()) {
            if (cc.containsColor(color)) {
                return cc;
            }
        }
        return null;
    }

    public boolean hasStaticSubclass(String id) {
        for (BasicColorClass cclass : colorClasses.values()) {
            if (cclass.hasStaticSubclass(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param id
     * @return the static subclass with the given id or null
     */
    public BasicColorClass getBasicColorClassOfStaticSubclass(String id) {
        for (BasicColorClass cclass : colorClasses.values()) {
            if (cclass.hasStaticSubclass(id)) {
                return cclass;
            }
        }
        return null;
    }

    public Color getSuccessorValue(Color color) throws NoSuchColorException, NoSuccessorForUnorderedColorClassException {
        for (BasicColorClass cc : colorClasses.values()) {
            Color ret = cc.getSuccessorValue(color);
            if (ret != null) {
                return ret;
            }
        }
        throw new NoSuchColorException("The color " + color.getId() + " is not existent in the Petri game '" + getName() + "'.");
    }

    // %%%%%%%%%%%%%%%%%%%%%%%% DELEGATES
    public double getXCoord(Node node) {
        return game.getXCoord(node);
    }

    public double getYCoord(Node node) {
        return game.getYCoord(node);
    }

    public boolean isSpecial(Place place) {
        return game.isSpecial(place);
    }

    public boolean isBad(Place place) {
        return game.isBad(place);
    }

    public boolean isReach(Place place) {
        return game.isReach(place);
    }

    public boolean isBuchi(Place place) {
        return game.isBuchi(place);
    }

    public boolean isStrongFair(Transition t) {
        return game.isStrongFair(t);
    }

    public boolean isWeakFair(Transition t) {
        return game.isWeakFair(t);
    }

    public boolean isEnvironment(Place place) {
        return game.isEnvironment(place);
    }

    public boolean isSystem(Place place) {
        return game.isSystem(place);
    }

    public Flow getFlow(String sourceId, String targetId) {
        return game.getFlow(sourceId, targetId);
    }

    public Flow getFlow(Node source, Node target) {
        return game.getFlow(source, target);
    }

    public Place getPlace(String id) {
        return game.getPlace(id);
    }

    public Transition getTransition(String id) {
        return game.getTransition(id);
    }

    public Set<Place> getPlaces() {
        return game.getPlaces();
    }

    public Set<Transition> getTransitions() {
        return game.getTransitions();
    }

    public String getName() {
        return game.getName();
    }

    public Node getNode(String id) {
        return game.getNode(id);
    }

    public Set<Flow> getEdges() {
        return game.getEdges();
    }

    public Set<Node> getNodes() {
        return game.getNodes();
    }

    public Set<Node> getPresetNodes(String id) {
        return game.getPresetNodes(id);
    }

    public Set<Node> getPostsetNodes(String id) {
        return game.getPostsetNodes(id);
    }

    public Set<Flow> getPresetEdges(String id) {
        return game.getPresetEdges(id);
    }

    public Set<Flow> getPostsetEdges(String id) {
        return game.getPostsetEdges(id);
    }

    public Set<Flow> getPostsetEdges(Node node) {
        return game.getPostsetEdges(node);
    }

    public Set<Node> getPostsetNodes(Node node) {
        return game.getPostsetNodes(node);
    }

    public Set<Flow> getPresetEdges(Node node) {
        return game.getPresetEdges(node);
    }

    public Set<Node> getPresetNodes(Node node) {
        return game.getPresetNodes(node);
    }

    public void setBad(Place place) {
        game.setBad(place);
    }

}
