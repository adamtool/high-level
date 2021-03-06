package uniolunisaar.adam.ds.synthesis.highlevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uniol.apt.adt.IGraphListener;
import uniol.apt.adt.exception.StructureException;
import uniol.apt.adt.extension.Extensible;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTupleElementType;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcType;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.SetMinusTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.predicate.Constants;
import uniolunisaar.adam.ds.synthesis.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.PredecessorTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;
import uniolunisaar.adam.ds.synthesis.pgwt.IPetriGame;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.ds.petrinet.PetriNetExtensionHandler;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetries;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.SymmetriesWithoutStoring;
import uniolunisaar.adam.ds.synthesis.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.exceptions.synthesis.highlevel.IdentifierAlreadyExistentException;
import uniolunisaar.adam.exceptions.synthesis.highlevel.NoNeighbourForUnorderedColorClassException;
import uniolunisaar.adam.exceptions.synthesis.highlevel.NoSuchColorDomainException;
import uniolunisaar.adam.exceptions.synthesis.highlevel.NoSuchColorException;
import uniolunisaar.adam.logic.synthesis.transformers.highlevel.HL2PGConverter;

/**
 * This class only provides the features for symmetric set-based high-level
 * Petri games, i.e., set-based high-level Petri games based on an underlying
 * symmetric high-level Petri net.
 *
 * For example we cannot use constants for arc expressions or transition
 * predicates. To allow this we have to replace the constant c by a variable x,
 * split up the corresponding color class into a singleton for this constant c
 * and the rest and add the predicate D(x)={c}. This leads to significantly less
 * symmetric behavior.
 *
 * @author Manuel Gieseking
 */
public class HLPetriGame extends Extensible implements IPetriGame {

    private final Map<String, BasicColorClass> colorClasses;
    private Iterable<Symmetry> symmetries;
    private final PetriGameWithTransits game;

    public HLPetriGame(HLPetriGame hlgame) {
        this.colorClasses = copyColorClasses(hlgame);
        this.game = new PetriGameWithTransits(hlgame.game);
    }

    public HLPetriGame(HLPetriGame hlgame, boolean byReference) {
        if (byReference) {
            this.colorClasses = hlgame.colorClasses;
            this.game = hlgame.game;
        } else {
            this.colorClasses = copyColorClasses(hlgame);
            this.game = new PetriGameWithTransits(hlgame.game);
        }
    }

    private Map<String, BasicColorClass> copyColorClasses(HLPetriGame hlgame) {
        Map<String, BasicColorClass> cclasses = new HashMap<>();
        for (Map.Entry<String, BasicColorClass> entry : hlgame.colorClasses.entrySet()) {
            String key = entry.getKey();
            BasicColorClass value = entry.getValue();
            cclasses.put(key, new BasicColorClass(value));
        }
        return cclasses;
    }

    public HLPetriGame(Map<String, BasicColorClass> colorClasses, PetriGameWithTransits game) {
        this.colorClasses = colorClasses;
        this.game = game;
    }

    public HLPetriGame(String name) {
        game = new PetriGameWithTransits(name);
        colorClasses = new HashMap<>();
    }

    public void createBasicColorClass(String id, boolean ordered, String... colors) throws IdentifierAlreadyExistentException {
        List<Color> cls = new ArrayList<>();
        for (int i = 0; i < colors.length; i++) {
            cls.add(new Color(colors[i]));
        }
        createBasicColorClass(id, ordered, cls);
    }

    public void createBasicColorClass(String id, boolean ordered, Color... colors) throws IdentifierAlreadyExistentException {
        createBasicColorClass(id, ordered, Arrays.asList(colors));
    }

    public void createBasicColorClass(String id, boolean ordered, List<Color> colors) throws IdentifierAlreadyExistentException {
        if (colorClasses.containsKey(id)) {
            throw new IdentifierAlreadyExistentException("The basic color identifier " + id + " already exists in the Petri game '" + game.getName() + "'.");
        }
        colorClasses.put(id, new BasicColorClass(id, ordered, colors));
    }

    public void createBasicColorClass(String id, boolean ordered, Pair<String, String[]>... staticSubClasses) throws IdentifierAlreadyExistentException {
//        List<Pair<String, String[]>> asdf =  new ArrayList<Pair<String, String[]>>(Arrays.staticSubClasses);
        createBasicColorClassByStaticSubClass(id, ordered, Arrays.asList(staticSubClasses));
    }

    public void createBasicColorClassByStaticSubClass(String id, boolean ordered, List<Pair<String, String[]>> staticSubClasses) throws IdentifierAlreadyExistentException {
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

    public void addStaticSubClasses(String basicColorClassId, List<Pair<String, String[]>> staticSubClasses) throws IdentifierAlreadyExistentException, IllegalStateException {
        BasicColorClass bcc = getBasicColorClass(basicColorClassId);
        if (!bcc.getStaticSubclasses().isEmpty()) {
            throw new IllegalStateException("The basic color class '" + basicColorClassId + " already has static subclasses in the Petri game '" + game.getName() + "'.");
        }
        Set<Color> addedColors = new HashSet<>();
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
            addedColors.addAll(cls);
            bcc.addStaticColorClass(subClassID, new StaticColorClass(subClassID, cls));
        }
        if (!addedColors.equals(new HashSet<>(bcc.getColors()))) {
            throw new IllegalStateException("The static subclasses dont make a partition of the colors in the basic color class '" + basicColorClassId + "'.");
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
                throw new NoSuchColorDomainException("The color domain " + domain.get(i) + " does not exist in the Petri net " + getName());
            }
        }
        HLPetriGameExtensionHandler.setColorClasses(p, domain);
    }

    public boolean hasColorTokens(Place p) {
        return HLPetriGameExtensionHandler.hasColorTokens(p);
    }

    public void setColorTokens(Place p, ColorTokens tokens) {
        HLPetriGameExtensionHandler.setColorTokens(p, tokens);
    }

    public void setColorTokens(Place p, String... colors) {
        ColorTokens tokens = new ColorTokens();
        for (String color : colors) {
            tokens.add(new ColorToken(new Color(color)));
        }
        setColorTokens(p, tokens);
    }

    public void setColorTokens(Place p, Color... colors) {
        ColorTokens tokens = new ColorTokens();
        for (Color color : colors) {
            ColorToken token = new ColorToken();
            token.add(color);
            tokens.add(token);
        }
        HLPetriGameExtensionHandler.setColorTokens(p, tokens);
    }

    public void setColorTokens(Place p, List<ColorToken> tokens) {
        ColorTokens tks = new ColorTokens(tokens);
        HLPetriGameExtensionHandler.setColorTokens(p, tks);
    }

    public ColorTokens getColorTokens(Place p) {
        if (HLPetriGameExtensionHandler.hasColorTokens(p)) {
            return HLPetriGameExtensionHandler.getColorTokens(p);
        } else {
            return null;
        }
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

    public void setPredicate(Transition t, IPredicate pred) {
        HLPetriGameExtensionHandler.setPredicate(t, pred);
    }

    public Set<Variable> getVariables(Transition t) {
        Set<Variable> vars = new HashSet<>();
        for (Flow presetEdge : t.getPresetEdges()) {
            ArcExpression expr = getArcExpression(presetEdge);
            vars.addAll(expr.getVariables());
        }
        vars.addAll(getPredicate(t).getVariables());
        for (Flow postsetEdge : t.getPostsetEdges()) {
            ArcExpression expr = getArcExpression(postsetEdge);
            vars.addAll(expr.getVariables());
        }
        return vars;
    }

// %%%%%%%%%%%%%%%%%%%% FLOWS   
    public boolean hasArcExpression(Flow f) {
        return HLPetriGameExtensionHandler.hasArcExpression(f);
    }

    public ArcExpression getArcExpression(Flow f) {
        return HLPetriGameExtensionHandler.getArcExpression(f);
    }

    public void setArcExpression(Flow f, ArcExpression expr) {
        HLPetriGameExtensionHandler.setArcExpression(f, expr);
    }

    public Flow createFlow(Node source, Node target) {
        Flow f = game.createFlow(source, target);
        HLPetriGameExtensionHandler.setArcExpression(f, new ArcExpression(new Variable("x")));
        return f;
    }

    public Flow createFlow(Node source, Node target, ArcExpression expr) {
        Flow f = game.createFlow(source, target);
        HLPetriGameExtensionHandler.setArcExpression(f, expr);
        return f;
    }

    public Collection<BasicColorClass> getBasicColorClasses() {
        return colorClasses.values();
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

    public Color getNeighbourValue(Color color, boolean pre) throws NoSuchColorException, NoNeighbourForUnorderedColorClassException {
        for (BasicColorClass cc : colorClasses.values()) {
            Color ret = cc.getNeighbourValue(color, pre);
            if (ret != null) {
                return ret;
            }
        }
        throw new NoSuchColorException("The color " + color.getId() + " is not existent in the Petri game '" + getName() + "'.");
    }

    public Valuations getValuations(Transition t) {
        // Get variable to color domain
        Map<Variable, List<Color>> var2CClass = new HashMap<>();
        try {
            for (Flow presetEdge : t.getPresetEdges()) {
                Place pre = presetEdge.getPlace();
                BasicColorClass[] bcs = getBasicColorClasses(pre);
                ArcExpression expr = getArcExpression(presetEdge);
                addVariableColorClassMapping(var2CClass, expr, bcs);
            }
            for (Flow postsetEdge : t.getPostsetEdges()) {
                Place post = postsetEdge.getPlace();
                BasicColorClass[] bcs = getBasicColorClasses(post);
                ArcExpression expr = getArcExpression(postsetEdge);
                addVariableColorClassMapping(var2CClass, expr, bcs);
            }
        } catch (StructureException e) {
            throw new StructureException(e.getMessage() + " for transition " + t.getId());
        }
        return new Valuations(var2CClass);
    }

    private void addVariableColorClassMapping(Map<Variable, List<Color>> var2CClass, ArcExpression expr, BasicColorClass[] bcs) {
        for (Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> expresssion : expr.getExpresssions()) {
            switch (expresssion.getFirst()) {
                case VARIABLE:
                    putVariable2ColorClassMapping(var2CClass, (Variable) expresssion.getSecond(), bcs[0].getColors());
                    break;
                case SUCCESSOR:
                    putVariable2ColorClassMapping(var2CClass, ((SuccessorTerm) expresssion.getSecond()).getVariable(), bcs[0].getColors());
                    break;
                case PREDECESSOR:
                    putVariable2ColorClassMapping(var2CClass, ((PredecessorTerm) expresssion.getSecond()).getVariable(), bcs[0].getColors());
                    break;
                case SETMINUS:
                    SetMinusTerm term = (SetMinusTerm) expresssion.getSecond();
                    for (Variable var : term.getVariables()) {
                        putVariable2ColorClassMapping(var2CClass, var, bcs[0].getColors());
                    }
                    break;
                case TUPLE: {
                    ArcTuple tuple = (ArcTuple) expresssion.getSecond();
                    int component = 0;
                    for (Iterator<Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>>> iterator = tuple.getValues().iterator(); iterator.hasNext();) {
                        Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>> value = iterator.next();
                        switch (value.getFirst()) {
                            case VARIABLE:
                                putVariable2ColorClassMapping(var2CClass, (Variable) value.getSecond(), bcs[component].getColors());
                                break;
                            case SUCCESSOR:
                                putVariable2ColorClassMapping(var2CClass, ((SuccessorTerm) value.getSecond()).getVariable(), bcs[component].getColors());
                                break;
                            case PREDECESSOR:
                                putVariable2ColorClassMapping(var2CClass, ((PredecessorTerm) value.getSecond()).getVariable(), bcs[component].getColors());
                                break;
                            case SETMINUS:
                                term = (SetMinusTerm) value.getSecond();
                                for (Variable var : term.getVariables()) {
                                    putVariable2ColorClassMapping(var2CClass, var, bcs[component].getColors());
                                }
                                break;
                        }
                        ++component;
                    }
                    break;
                }
            }
        }
    }

    private void putVariable2ColorClassMapping(Map<Variable, List<Color>> var2CClass, Variable var, List<Color> colors) {
        List<Color> oldColors = var2CClass.put(var, colors);
        if (oldColors != null && !oldColors.equals(colors)) {
            throw new StructureException("The variable '" + var + "' is mapped to two different color classes '" + colors.toString() + "' and '" + oldColors.toString() + "'");
        }
    }

    // TODO: Only needed for the eventuallyEnabled method below
    // and this is only needed for the nondeterminism. All in all
    // this is totally stupid, since when I calculate it anyhow, 
    // I could also used in the other cases and this results in
    // the LLApproach ...
    private PetriGameWithTransits llGame = null;

    public boolean eventuallyEnabled(ColoredTransition t1, ColoredTransition t2) {
        // TODO: this is really silly. If this approach has any advantages,
        // I have to write this eventually enabledness by using only the 
        // high-level structure
        if (llGame == null) {
            llGame = HL2PGConverter.convert(this);
        }
        Transition llt1 = HL2PGConverter.getTransition(llGame, t1.getTransition().getId(), t1.getVal());
        Transition llt2 = HL2PGConverter.getTransition(llGame, t2.getTransition().getId(), t2.getVal());
        return llGame.eventuallyEnabled(llt1, llt2);
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
// TODO: not directly allow users to modify the sets!

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

    public void setReach(Place place) {
        game.setReach(place);
    }

    public void setBuchi(Place place) {
        game.setBuchi(place);
    }

    public String getProcessFamilyID() {
        return PetriNetExtensionHandler.getProcessFamilyID(game);
    }

    protected boolean addListener(IGraphListener<PetriNet, Flow, Node> listener) {
        return game.addListener(listener);
    }

    protected Map<String, BasicColorClass> getColorClasses() {
        return colorClasses;
    }

    protected PetriGameWithTransits getGame() {
        return game;
    }

    @Override
    public void initializeWinningCondition(Condition<?> winCon) {
        winCon.buffer(game);
    }

    public boolean storeSymmetries = true;

    /**
     * Attention: if you later changed the basic color classes, the symmetries
     * may possibly not fit anymore.
     *
     * @return
     */
    public Iterable<Symmetry> getSymmetries() {
        if (symmetries == null) {
            if (storeSymmetries) {
                symmetries = new Symmetries(getBasicColorClasses());
            } else {
                symmetries = new SymmetriesWithoutStoring(getBasicColorClasses());
            }
        }
        return symmetries;
    }
}
