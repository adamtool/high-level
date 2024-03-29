package uniolunisaar.adam.logic.synthesis.transformers.highlevel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import uniol.apt.adt.exception.FlowExistsException;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.synthesis.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.synthesis.highlevel.Color;
import uniolunisaar.adam.ds.synthesis.highlevel.ColorDomain;
import uniolunisaar.adam.ds.synthesis.highlevel.ColorToken;
import uniolunisaar.adam.ds.synthesis.highlevel.ColorTokens;
import uniolunisaar.adam.ds.synthesis.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;
import uniolunisaar.adam.ds.synthesis.highlevel.ValuationIterator;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuations;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcTupleElementType;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.IArcType;
import uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions.SetMinusType;
import uniolunisaar.adam.ds.synthesis.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.ColorClassType;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.Variable;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.synthesis.pgwt.PetriGameWithTransits;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.ConcurrencyPreservingCalculator;
import uniolunisaar.adam.logic.synthesis.pgwt.calculators.MaxTokenCountCalculator;
import uniolunisaar.adam.tools.CartesianProduct;
import uniolunisaar.adam.util.AdamHLExtensions;
import uniolunisaar.adam.util.ExtensionManagement;
import uniolunisaar.adam.util.PGTools;

/**
 *
 * @author Manuel Gieseking
 */
public class HL2PGConverter {

    private static final String ID_DELIM = "_"; // TODO: think of s.th. better on the one hand readable by APT on the other not already existing?
    private static final String COLOR_DELIM = "x";
    private static final String VALUATION_DELIM = "_";

    /**
     * Could have problems iff ID_DELIM is also used for other things in the
     * llPlaceID.
     *
     * @param llPlaceID
     * @return
     */
    @Deprecated
    public static String getHLPlaceID(String llPlaceID) {
        return llPlaceID.substring(0, llPlaceID.indexOf(ID_DELIM) - 1);
    }

    /**
     * Could have problems iff ID_DELIM or COLOR_DELIM is also used for other
     * things in the llPlaceID.
     *
     * @param llPlaceID
     * @return
     */
    @Deprecated
    public static String[] getPlaceColorIDs(String llPlaceID) {
        return llPlaceID.substring(llPlaceID.indexOf(ID_DELIM) + 1).split(COLOR_DELIM);
    }

    /**
     * Could have problems iff ID_DELIM is also used for other things in the
     * llTransitionID.
     *
     * @param llTransitionID
     * @return
     */
    @Deprecated
    public static String getHLTransitionID(String llTransitionID) {
        return llTransitionID.substring(0, llTransitionID.indexOf(ID_DELIM) - 1);
    }

    public static String getPlaceID(String origID, Color... colors) {
        return getPlaceID(origID, Arrays.asList(colors));
    }

    public static String getPlaceID(String origID, List<Color> colors) {
        StringBuilder sb = new StringBuilder(origID);
        sb.append(ID_DELIM);
        for (int i = 0; i < colors.size() - 1; i++) {
            sb.append(colors.get(i)).append(COLOR_DELIM);
        }
        if (colors.size() >= 1) {
            sb.append(colors.get(colors.size() - 1));
        }
        return sb.toString();
    }

    public static String getPlaceID(String origID, ColorToken token) {
        StringBuilder sb = new StringBuilder(origID);
        sb.append(ID_DELIM);
        for (int i = 0; i < token.size() - 1; i++) {
            sb.append(token.get(i)).append(COLOR_DELIM);
        }
        if (token.size() >= 1) {
            sb.append(token.get(token.size() - 1));
        }
        return sb.toString();
    }

    /**
     * This sorting is quite expensive because we use it for applying the
     * symmetries a lot. Did not check whether in this case it would be better
     * to use a TreeMap (add/remove/contains in log(n) (sort ArrayList
     * n*log(n))). We cannot use the hash code because there is no guarantee
     * that there a now collisions.
     *
     * @param val
     * @return
     */
    private static String valToTransitionIdentifier(Valuation val) {
        StringBuilder sb = new StringBuilder();
        // sort the valuation first
        TreeMap<Variable, Color> sorted = val.getSorted(); // todo: maybe think of s.th. better? Could be quite expensive. Possibly better to let Valuation directly be sorted?
        for (Map.Entry<Variable, Color> entry : sorted.entrySet()) {
            Variable key = entry.getKey();
            Color value = entry.getValue();
            //todo: this is not the nicest output, but allows the get it read by APT after rendering...
            sb.append(key.toString()).append(VALUATION_DELIM).append(value.toString());
        }
        return sb.toString();
    }

    /**
     * It's kind of expensive, due to the ordering of the valuation.
     * @param origID
     * @param val
     * @return 
     */
    public static String calculateTransitionID(String origID, Valuation val) {
        return origID.concat(ID_DELIM) + valToTransitionIdentifier(val);
    }

    public static Transition getTransition(PetriNet llPetriGame, String origID, Valuation val) {
        return ((Map<TransitionKey, Transition>) ExtensionManagement.getInstance().getExtension(llPetriGame, AdamHLExtensions.convTransitionMapping, Map.class))
                .get(new TransitionKey(origID, val));
    }

    public static void setColorsAndID2Extension(Place llPlace, String origID, List<Color> colors) {
        ExtensionManagement.getInstance().putExtension(llPlace, AdamHLExtensions.convOrigID, origID);
        ExtensionManagement.getInstance().putExtension(llPlace, AdamHLExtensions.convColors, colors);
    }

    public static String getOrigID(Place llPlace) {
        return ExtensionManagement.getInstance().getExtension(llPlace, AdamHLExtensions.convOrigID, String.class);
    }

    public static List<Color> getColors(Place llPlace) {
        return ExtensionManagement.getInstance().getExtension(llPlace, AdamHLExtensions.convColors, List.class);
    }

    public static void setValuationAndID2Extension(Transition lltransition, String origID, Valuation val) {
        ExtensionManagement.getInstance().putExtension(lltransition, AdamHLExtensions.convOrigID, origID);
        ExtensionManagement.getInstance().putExtension(lltransition, AdamHLExtensions.convValuation, val);
    }

    public static String getOrigID(Transition llTransition) {
        return ExtensionManagement.getInstance().getExtension(llTransition, AdamHLExtensions.convOrigID, String.class);
    }

    public static Valuation getValuation(Transition llTransition) {
        return ExtensionManagement.getInstance().getExtension(llTransition, AdamHLExtensions.convValuation, Valuation.class);
    }

    public static int getHashCode(Place llPlace) {
        int result = getOrigID(llPlace).hashCode();
        result = 17 * result * Objects.hashCode(getColors(llPlace));
        return result;
    }

    public static int getHashCode(Transition llTransition) {
        int result = getOrigID(llTransition).hashCode();
        result = 11 * result * Objects.hashCode(getValuation(llTransition));
        return result;
    }

    public static PetriGameWithTransits convert(HLPetriGame hlgame) {
        return convert(hlgame, false);
    }

    public static PetriGameWithTransits convert(HLPetriGame hlgame, boolean save2Extension) {
        return convert(hlgame, save2Extension, false);
    }

    public static PetriGameWithTransits convert(HLPetriGame hlgame, boolean save2Extension, boolean withCalculators) {
        PetriGameWithTransits pg;
        if (withCalculators) {
            pg = new PetriGameWithTransits(hlgame.getName() + " - LL-Version", new ConcurrencyPreservingCalculator(), new MaxTokenCountCalculator());
        } else {
            pg = new PetriGameWithTransits(hlgame.getName() + " - LL-Version");
        }
        PGTools.setConditionAnnotation(pg, Condition.Objective.A_SAFETY); // TODO: do it properly
        // Places
        addPlaces(hlgame, pg, save2Extension);
        // set initial marking
        setInitialMarking(hlgame, pg);
        // transitions
        addTransitions(hlgame, pg, save2Extension);
        if (hlgame.hasExtension("partitions")) { // todo: do it better with the key
            Map<String, Integer> partitions = (Map<String, Integer>) hlgame.getExtension("partitions");
            for (Place place : pg.getPlaces()) {
                if (pg.isEnvironment(place)) {
                    pg.setPartition(place, 0);
                }
                if (partitions.containsKey(place.getId())) {
                    pg.setPartition(place, partitions.get(place.getId()));
                }
            }
        }
        return pg;
    }

    private static void addPlaces(HLPetriGame hlgame, PetriGameWithTransits pg, boolean save2Extension) {
        for (Place place : hlgame.getPlaces()) {
            ColorDomain dom = hlgame.getColorDomain(place);
            boolean env = hlgame.isEnvironment(place);
            boolean special = hlgame.isSpecial(place);
            // EXPLICIT VERSION FOR SIZES 1 AND 2 (not needed anymore, programmed the cartesian product)
//            if (dom.size() <= 1) {
//                BasicColorClass bcc = hlgame.getBasicColorClass(dom.get(0));
//                for (Color color : bcc.getColors()) {
//                    Place p = pg.createPlace(getPlaceID(place.getId(), color));
//                    if (env) {
//                        pg.setEnvironment(p);
//                    } else {
//                        pg.setSystem(p);
//                    }
//                    if (special) {
//                        pg.setBad(p);
//                    }
//                }
//            } else if (dom.size() == 2) {
////                createPlace(dom, 0, place.getId(), env, hlgame, pg);
//                BasicColorClass bcc1 = hlgame.getBasicColorClass(dom.get(0));
//                BasicColorClass bcc2 = hlgame.getBasicColorClass(dom.get(1));
//                for (Color c1 : bcc1.getColors()) {
//                    for (Color c2 : bcc2.getColors()) {
//                        Place p = pg.createPlace(getPlaceID(place.getId(), c1, c2));
//                        if (env) {
//                            pg.setEnvironment(p);
//                        } else {
//                            pg.setSystem(p);
//                        }
//                        if (special) {
//                            pg.setBad(p);
//                        }
//                    }
//                }
//            } else {
//                throw new UnsupportedOperationException("Color domains with more than two basic color classes are not yet supported.");
//            }
            // NEW VERSION USING THE CARTESIAN PRODUCT
            List<List<Color>> colorClasses = new ArrayList<>();
            for (int i = 0; i < dom.size(); i++) {
                colorClasses.add(hlgame.getBasicColorClass(dom.get(i)).getColors());
            }
            CartesianProduct<Color> prod = new CartesianProduct<>(colorClasses);
            for (Iterator<List<Color>> it = prod.iterator(); it.hasNext();) {
                List<Color> colors = it.next();
                Place p = pg.createPlace(getPlaceID(place.getId(), colors));
                if (save2Extension) {
                    setColorsAndID2Extension(p, place.getId(), new ArrayList<>(colors));
                }
                if (env) {
                    pg.setEnvironment(p);
                } else {
                    pg.setSystem(p);
                }
                if (special) {
                    pg.setBad(p);
                }
            }
        }
    }

    private static void setInitialMarking(HLPetriGame hlgame, PetriGameWithTransits pg) {
        for (Place place : hlgame.getPlaces()) {
            if (hlgame.hasColorTokens(place)) {
                ColorTokens tokens = hlgame.getColorTokens(place);
                for (ColorToken token : tokens) {
                    Place p = pg.getPlace(getPlaceID(place.getId(), token));
                    p.setInitialToken(1);
                }
            }
        }
    }

    private static void addTransitions(HLPetriGame hlgame, PetriGameWithTransits pg, boolean save2Extension) {
        Map<TransitionKey, Transition> transitionMapping = new HashMap<>();
        for (Transition t : hlgame.getTransitions()) {
            // For every valuation create a transition
            Valuations vals = hlgame.getValuations(t);
            for (ValuationIterator it = vals.iterator(); it.hasNext();) {
                Valuation val = it.next();
                IPredicate pred = hlgame.getPredicate(t);
                if (pred.check(val)) { // only when the valuation satisfies the predicate                        
                    // Create the transition
                    String id = calculateTransitionID(t.getId(), val);
                    Transition tLL = pg.createTransition(id);
                    transitionMapping.put(new TransitionKey(t.getId(), val), tLL);
                    if (save2Extension) {
                        setValuationAndID2Extension(tLL, t.getId(), new Valuation(val));
                    }
                    // create the flows
                    for (Flow presetEdge : t.getPresetEdges()) {
                        createFlows(tLL, presetEdge, val, hlgame, pg, true);
                    }
                    for (Flow postsetEdge : t.getPostsetEdges()) {
                        createFlows(tLL, postsetEdge, val, hlgame, pg, false);
                    }
                }
            }
        }
        ExtensionManagement.getInstance().putExtension(pg, AdamHLExtensions.convTransitionMapping, transitionMapping);
    }

    private static void createFlows(Transition tLL, Flow flowHL, Valuation val, HLPetriGame hlgame, PetriGameWithTransits pg, boolean pre) {
        String origID = flowHL.getPlace().getId();
        ArcExpression expr = hlgame.getArcExpression(flowHL);
        for (Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> expression : expr.getExpresssions()) {
            switch (expression.getFirst()) {
                case VARIABLE: // this creates kind of VARIABLE || SUCCESSOR                                         
                case SUCCESSOR: {
                    Color col = (Color) expression.getSecond().getValue(val);
                    Place place = pg.getPlace(getPlaceID(origID, col));
                    if (pre) {
                        createFlow(place, tLL, pg);
                    } else {
                        createFlow(tLL, place, pg);
                    }
                    break;
                }
                case COLORCLASS: {
                    ColorClassType colors = (ColorClassType) expression.getSecond().getValue(val);
                    BasicColorClass bc = hlgame.getBasicColorClass(colors.getId());
                    for (Color color : bc.getColors()) {
                        Place place = pg.getPlace(getPlaceID(origID, color));
                        if (pre) {
                            createFlow(place, tLL, pg);
                        } else {
                            createFlow(tLL, place, pg);
                        }
                    }
                    break;
                }
                case SETMINUS: {
                    SetMinusType setminusType = (SetMinusType) expression.getSecond().getValue(val);
                    BasicColorClass bc = hlgame.getBasicColorClass(setminusType.getColorClass().getId());
                    List<Color> cls = new ArrayList<>(bc.getColors());
                    cls.removeAll(setminusType.getColors());
                    for (Color color : cls) {
                        Place place = pg.getPlace(getPlaceID(origID, color));
                        if (pre) {
                            createFlow(place, tLL, pg);
                        } else {
                            createFlow(tLL, place, pg);
                        }
                    }
                    break;
                }
                case TUPLE: {
                    ArcTuple tuple = (ArcTuple) expression.getSecond();
                    List<Color> colors = new ArrayList<>();
                    List<List<Color>> colorClasses = new ArrayList<>();
                    List<Integer> idxs = new ArrayList<>();
                    int component = 0;
                    for (Iterator<Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>>> iterator = tuple.getValues().iterator(); iterator.hasNext();) {
                        Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>> value = iterator.next();
                        switch (value.getFirst()) {
                            case VARIABLE:
                            case SUCCESSOR: {
                                colors.add((Color) value.getSecond().getValue(val));
                                break;
                            }
                            case COLORCLASS: {
                                ColorClassType colorsSet = (ColorClassType) value.getSecond().getValue(val);
                                BasicColorClass bc = hlgame.getBasicColorClass(colorsSet.getId());
                                colorClasses.add(bc.getColors());
                                idxs.add(component);
                                break;
                            }
                            case SETMINUS: {
                                SetMinusType setminusType = (SetMinusType) value.getSecond().getValue(val);
                                BasicColorClass bc = hlgame.getBasicColorClass(setminusType.getColorClass().getId());
                                List<Color> cls = new ArrayList<>(bc.getColors());
                                cls.removeAll(setminusType.getColors());
                                colorClasses.add(cls);
                                idxs.add(component);
                                break;
                            }
                        }
                        ++component;
                    }
                    if (colorClasses.isEmpty()) {
                        Place place = pg.getPlace(getPlaceID(origID, colors));
                        if (pre) {
                            createFlow(place, tLL, pg);
                        } else {
                            createFlow(tLL, place, pg);
                        }
                    } else {
                        CartesianProduct<Color> prod = new CartesianProduct<>(colorClasses);
                        for (Iterator<List<Color>> it = prod.iterator(); it.hasNext();) {
                            List<Color> ctuple = it.next();
                            // create the correctly ordered color list
                            List<Color> newColors = new ArrayList<>();
                            int varIdx = 0;
                            int classIdx = 0;
                            for (int i = 0; i < colors.size() + ctuple.size(); i++) {
                                if (idxs.contains(i)) {
                                    newColors.add(ctuple.get(classIdx++));
                                } else {
                                    newColors.add(colors.get(varIdx++));
                                }
                            }
                            Place place = pg.getPlace(getPlaceID(origID, newColors));
                            if (pre) {
                                createFlow(place, tLL, pg);
                            } else {
                                createFlow(tLL, place, pg);
                            }
                        }
                    }
                }
            }
        }
    }

    private static void createFlow(Node pre, Node post, PetriGameWithTransits pg) {
        try { // TODO: replace this when addded a containsFlow method to APT
            pg.createFlow(pre, post);
        } catch (FlowExistsException e) { // not nice but APT currently has no containsFlow method.
            Flow f = pg.getFlow(pre, post);
            f.setWeight(f.getWeight() + 1);
        }
    }

}
