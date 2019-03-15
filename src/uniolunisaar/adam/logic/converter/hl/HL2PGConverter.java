package uniolunisaar.adam.logic.converter.hl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
import uniolunisaar.adam.ds.objectives.Condition;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.tools.CartesianProduct;
import uniolunisaar.adam.util.PNWTTools;

/**
 *
 * @author Manuel Gieseking
 */
public class HL2PGConverter {

    private static final String ID_DELIM = "_"; // TODO: think of s.th. better on the one hand readable by APT on the other not already existing?
    private static final String COLOR_DELIM = "x";

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

    public static String getTransitionID(String origID, Valuation val) {
        return origID + ID_DELIM + val.toTransitionIdentifier();
    }

    public static PetriGame convert(HLPetriGame hlgame) {
        PetriGame pg = new PetriGame(hlgame.getName() + " - LL-Version");
        PNWTTools.setConditionAnnotation(pg, Condition.Objective.A_SAFETY); // TODO: do it properly
        // Places
        addPlaces(hlgame, pg);
        // set initial marking
        setInitialMarking(hlgame, pg);
        // transitions
        addTransitions(hlgame, pg);
        return pg;
    }

    private static void addPlaces(HLPetriGame hlgame, PetriGame pg) {
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
                Place p = pg.createPlace(getPlaceID(place.getId(), it.next()));
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

    private static void addTransitions(HLPetriGame hlgame, PetriGame pg) {
        for (Transition t : hlgame.getTransitions()) {
            // For every valuation create a transition
            Valuations vals = hlgame.getValuations(t);
            for (ValuationIterator it = vals.iterator(); it.hasNext();) {
                Valuation val = it.next();
                IPredicate pred = hlgame.getPredicate(t);
                if (pred.check(val)) { // only when the valuation satisfies the predicate                        
                    // Create the transition
                    Transition tLL = pg.createTransition(getTransitionID(t.getId(), val));
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
