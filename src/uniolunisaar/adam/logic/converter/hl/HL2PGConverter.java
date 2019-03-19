package uniolunisaar.adam.logic.converter.hl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import uniol.apt.adt.exception.FlowExistsException;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.BasicColorClass;
import uniolunisaar.adam.ds.highlevel.Color;
import uniolunisaar.adam.ds.highlevel.ColorDomain;
import uniolunisaar.adam.ds.highlevel.ColorToken;
import uniolunisaar.adam.ds.highlevel.ColorTokens;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.ValuationIterator;
import uniolunisaar.adam.ds.highlevel.Valuations;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTupleElementType;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcType;
import uniolunisaar.adam.ds.highlevel.arcexpressions.SetMinusType;
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassType;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.tools.CartesianProduct;
import uniolunisaar.adam.util.AdamExtensions;
import uniolunisaar.adam.util.PNWTTools;

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

    public static String valToTransitionIdentifier(Valuation val) {
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

    public static String getTransitionID(String origID, Valuation val) {
        return origID + ID_DELIM + valToTransitionIdentifier(val);
    }

    public static void setColorsAndID2Extension(Place llPlace, String origID, List<Color> colors) {
        llPlace.putExtension(AdamExtensions.convOrigID.name(), origID);
        llPlace.putExtension(AdamExtensions.convColors.name(), colors);
    }

    public static String getOrigID(Place llPlace) {
        return (String) llPlace.getExtension(AdamExtensions.convOrigID.name());
    }

    public static List<Color> getColors(Place llPlace) {
        return (List<Color>) llPlace.getExtension(AdamExtensions.convColors.name());
    }

    public static void setValuationAndID2Extension(Transition lltransition, String origID, Valuation val) {
        lltransition.putExtension(AdamExtensions.convOrigID.name(), origID);
        lltransition.putExtension(AdamExtensions.convValuation.name(), val);
    }

    public static String getOrigID(Transition llTransition) {
        return (String) llTransition.getExtension(AdamExtensions.convOrigID.name());
    }

    public static Valuation getValuation(Transition llTransition) {
        return (Valuation) llTransition.getExtension(AdamExtensions.convValuation.name());
    }

    public static PetriGame convert(HLPetriGame hlgame) {
        return convert(hlgame, false);
    }

    public static PetriGame convert(HLPetriGame hlgame, boolean save2Extension) {
        PetriGame pg = new PetriGame(hlgame.getName() + " - LL-Version");
        PNWTTools.setConditionAnnotation(pg, Condition.Objective.A_SAFETY); // TODO: do it properly
        // Places
        addPlaces(hlgame, pg, save2Extension);
        // set initial marking
        setInitialMarking(hlgame, pg);
        // transitions
        addTransitions(hlgame, pg, save2Extension);
        return pg;
    }

    private static void addPlaces(HLPetriGame hlgame, PetriGame pg, boolean save2Extension) {
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

    private static void setInitialMarking(HLPetriGame hlgame, PetriGame pg) {
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

    private static void addTransitions(HLPetriGame hlgame, PetriGame pg, boolean save2Extension) {
        for (Transition t : hlgame.getTransitions()) {
            // For every valuation create a transition
            Valuations vals = hlgame.getValuations(t);
            for (ValuationIterator it = vals.iterator(); it.hasNext();) {
                Valuation val = it.next();
                IPredicate pred = hlgame.getPredicate(t);
                if (pred.check(val)) { // only when the valuation satisfies the predicate                        
                    // Create the transition
                    Transition tLL = pg.createTransition(getTransitionID(t.getId(), val));
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
    }

    private static void createFlows(Transition tLL, Flow flowHL, Valuation val, HLPetriGame hlgame, PetriGame pg, boolean pre) {
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
                    cls.remove((Color) setminusType.getColor());
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
                                cls.remove((Color) setminusType.getColor());
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

    private static void createFlow(Node pre, Node post, PetriGame pg) {
        try { // TODO: replace this when addded a containsFlow method to APT
            pg.createFlow(pre, post);
        } catch (FlowExistsException e) { // not nice but APT currently has no containsFlow method.
            Flow f = pg.getFlow(pre, post);
            f.setWeight(f.getWeight() + 1);
        }
    }

}
