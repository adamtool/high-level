package uniolunisaar.adam.logic.converter.hl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import uniol.apt.adt.pn.Flow;
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
import uniolunisaar.adam.ds.highlevel.predicate.IPredicate;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassType;
import uniolunisaar.adam.ds.highlevel.terms.SuccessorTerm;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.ds.petrigame.PetriGame;
import uniolunisaar.adam.tools.CartesianProduct;

/**
 *
 * @author Manuel Gieseking
 */
public class HL2PGConverter {

    private static final String ID_DELIM = "-";
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

    public static String getTransitionID(String origID, Valuation val) {
        return origID + ID_DELIM + val.toString();
    }

    public static PetriGame convert(HLPetriGame hlgame) {
        PetriGame pg = new PetriGame(hlgame.getName() + " - LL-Version");
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
            if (dom.size() <= 1) {
                BasicColorClass bcc = hlgame.getBasicColorClass(dom.get(0));
                for (Color color : bcc.getColors()) {
                    Place p = pg.createPlace(getPlaceID(place.getId(), color));
                    if (env) {
                        pg.setEnvironment(p);
                    } else {
                        pg.setSystem(p);
                    }
                    if (special) {
                        pg.setBad(p);
                    }
                }
            } else if (dom.size() == 2) {
//                createPlace(dom, 0, place.getId(), env, hlgame, pg);
                BasicColorClass bcc1 = hlgame.getBasicColorClass(dom.get(0));
                BasicColorClass bcc2 = hlgame.getBasicColorClass(dom.get(1));
                for (Color c1 : bcc1.getColors()) {
                    for (Color c2 : bcc2.getColors()) {
                        Place p = pg.createPlace(getPlaceID(place.getId(), c1, c2));
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
            } else {
                throw new UnsupportedOperationException("Color domains with more than two basic color classes are not yet supported.");
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
            // Get variable to color domain
            Map<Variable, List<Color>> var2CClass = new HashMap<>();
            for (Flow presetEdge : t.getPresetEdges()) {
                Place pre = presetEdge.getPlace();
                BasicColorClass[] bcs = hlgame.getBasicColorClasses(pre);
                ArcExpression expr = hlgame.getArcExpression(presetEdge);
                addVariableColorClassMapping(var2CClass, expr, bcs);
            }
            for (Flow postsetEdge : t.getPostsetEdges()) {
                Place post = postsetEdge.getPlace();
                BasicColorClass[] bcs = hlgame.getBasicColorClasses(post);
                ArcExpression expr = hlgame.getArcExpression(postsetEdge);
                addVariableColorClassMapping(var2CClass, expr, bcs);
            }
            // For every valuation create a transition
            Valuations vals = new Valuations(var2CClass);
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

    private static void addVariableColorClassMapping(Map<Variable, List<Color>> var2CClass, ArcExpression expr, BasicColorClass[] bcs) {
        for (Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> expresssion : expr.getExpresssions()) {
            switch (expresssion.getFirst()) {
                case VARIABLE:
                    var2CClass.put((Variable) expresssion.getSecond(), bcs[0].getColors());
                    break;
                case SUCCESSOR:
                    var2CClass.put(((SuccessorTerm) expresssion.getSecond()).getVariable(), bcs[0].getColors());
                    break;
                case TUPLE:
                    ArcTuple tuple = (ArcTuple) expresssion.getSecond();
                    int component = 0;
                    for (Iterator<Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>>> iterator = tuple.getValues().iterator(); iterator.hasNext();) {
                        Pair<IArcTupleElement.Sort, IArcTupleElement<? extends IArcTupleElementType>> value = iterator.next();
                        switch (value.getFirst()) {
                            case VARIABLE:
                                var2CClass.put((Variable) value.getSecond(), bcs[component].getColors());
                                break;
                            case SUCCESSOR:
                                var2CClass.put(((SuccessorTerm) value.getSecond()).getVariable(), bcs[component].getColors());
                                break;
                        }
                        ++component;
                    }
            }
        }
    }

    private static void createFlows(Transition tLL, Flow flowHL, Valuation val, HLPetriGame hlgame, PetriGame pg, boolean pre) {
        String origID = flowHL.getPlace().getId();
        ArcExpression expr = hlgame.getArcExpression(flowHL);
        for (Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> expresssion : expr.getExpresssions()) {
            switch (expresssion.getFirst()) {
                case VARIABLE: // this creates kind of VARIABLE || SUCCESSOR                                         
                case SUCCESSOR: {
                    Color col = (Color) expresssion.getSecond().getValue(val);
                    Place place = pg.getPlace(getPlaceID(origID, col));
                    if (pre) {
                        pg.createFlow(place, tLL);
                    } else {
                        pg.createFlow(tLL, place);
                    }
                    break;
                }
                case COLORCLASS: {
                    ColorClassType colors = (ColorClassType) expresssion.getSecond().getValue(val);
                    BasicColorClass bc = hlgame.getBasicColorClass(colors.getId());
                    for (Color color : bc.getColors()) {
                        Place place = pg.getPlace(getPlaceID(origID, color));
                        if (pre) {
                            pg.createFlow(place, tLL);
                        } else {
                            pg.createFlow(tLL, place);
                        }
                    }
                    break;
                }
                case TUPLE: {
                    ArcTuple tuple = (ArcTuple) expresssion.getSecond();
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
                        }
                        ++component;
                    }
                    if (colorClasses.isEmpty()) {
                        Place place = pg.getPlace(getPlaceID(origID, colors));
                        if (pre) {
                            pg.createFlow(place, tLL);
                        } else {
                            pg.createFlow(tLL, place);
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
                                pg.createFlow(place, tLL);
                            } else {
                                pg.createFlow(tLL, place);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * TODO: Finish this method which creates the places of the cartesian
     * product for an arbitrary number of basic color classes.
     *
     * @param dom
     * @param idx
     * @param placeName
     * @param env
     * @param hlgame
     * @param pg
     */
    private static void createPlace(ColorDomain dom, int idx, String placeName, boolean env, HLPetriGame hlgame, PetriGame pg) {
        if (idx == dom.size()) {
            Place p = pg.createPlace(placeName);
            if (env) {
                pg.setEnvironment(p);
            } else {
                pg.setSystem(p);
            }
        } else {
            BasicColorClass bcc = hlgame.getBasicColorClass(dom.get(0));
            for (Color color : bcc.getColors()) {

            }
        }
    }

}
