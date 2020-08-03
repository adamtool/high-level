package uniolunisaar.adam.ds.highlevel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Transition;
import uniol.apt.util.Pair;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcExpression;
import uniolunisaar.adam.ds.highlevel.arcexpressions.ArcTuple;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTerm;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTupleElement;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcTupleElementType;
import uniolunisaar.adam.ds.highlevel.arcexpressions.IArcType;
import uniolunisaar.adam.ds.highlevel.arcexpressions.SetMinusType;
import uniolunisaar.adam.ds.highlevel.symmetries.Symmetry;
import uniolunisaar.adam.ds.highlevel.terms.ColorClassType;
import uniolunisaar.adam.ds.highlevel.terms.Variable;
import uniolunisaar.adam.tools.CartesianProduct;

/**
 *
 * @author Manuel Gieseking
 */
public class ColoredTransition implements ITransition<ColoredPlace> {

    private final Transition transition;
    private final Valuation val;
    private final HLPetriGame hlgame;

    public ColoredTransition(ColoredTransition t) {
        hlgame = t.hlgame;
        transition = t.transition;
        val = new Valuation(t.val);
    }

    public ColoredTransition(HLPetriGame hlgame, Transition transition, Valuation val) {
        this.hlgame = hlgame;
        this.transition = transition;
        this.val = val;
    }

//    public void apply(Symmetry sym) {
//        for (Map.Entry<Variable, Color> entry : val.entrySet()) {
//            Variable var = entry.getKey();
//            Color c = entry.getValue();
//            val.put(var, sym.get(c));
//        }
//    }
    public ColoredTransition apply(Symmetry sym) {
        Valuation newVal = new Valuation();
        for (Map.Entry<Variable, Color> entry : val.entrySet()) {
            Variable var = entry.getKey();
            Color c = entry.getValue();
            newVal.put(var, sym.get(c));
        }
        return new ColoredTransition(hlgame, transition, newVal);
    }

    public Transition getTransition() {
        return transition;
    }

    public Valuation getVal() {
        return val;
    }

    public boolean isValid() {
        return hlgame.getPredicate(transition).check(val);
    }

    @Override
    public Set<ColoredPlace> getPreset() {
        Set<ColoredPlace> ret = new HashSet<>();
        for (Flow f : transition.getPresetEdges()) {
            ret.addAll(getPlaces(f));
        }
        return ret;
    }

    @Override
    public Set<ColoredPlace> getPostset() {
        Set<ColoredPlace> ret = new HashSet<>();
        for (Flow f : transition.getPostsetEdges()) {
            ret.addAll(getPlaces(f));
        }
        return ret;
    }

    private Set<ColoredPlace> getPlaces(Flow f) {
        Set<ColoredPlace> ret = new HashSet<>();
        ArcExpression expr = hlgame.getArcExpression(f);
        for (Pair<IArcTerm.Sort, IArcTerm<? extends IArcType>> expression : expr.getExpresssions()) {
            switch (expression.getFirst()) {
                case VARIABLE: // this creates kind of VARIABLE || SUCCESSOR                                         
                case SUCCESSOR: {
                    Color col = (Color) expression.getSecond().getValue(val);
                    ret.add(new ColoredPlace(f.getPlace(), new ColorToken(col)));
                    break;
                }
                case COLORCLASS: {
                    ColorClassType colors = (ColorClassType) expression.getSecond().getValue(val);
                    BasicColorClass bc = hlgame.getBasicColorClass(colors.getId());// yodo: here possibly also static subclass
                    for (Color color : bc.getColors()) {
                        ret.add(new ColoredPlace(f.getPlace(), new ColorToken(color)));
                    }
                    break;
                }
                case SETMINUS: {
                    SetMinusType setminusType = (SetMinusType) expression.getSecond().getValue(val);
                    BasicColorClass bc = hlgame.getBasicColorClass(setminusType.getColorClass().getId());
                    List<Color> cls = new ArrayList<>(bc.getColors());
                    cls.removeAll(setminusType.getColors());
                    for (Color color : cls) {
                        ret.add(new ColoredPlace(f.getPlace(), new ColorToken(color)));
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
                        ret.add(new ColoredPlace(f.getPlace(), new ColorToken(colors)));
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
                            ret.add(new ColoredPlace(f.getPlace(), new ColorToken(newColors)));
                        }
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash * Objects.hashCode(this.transition);
        hash = 59 * hash * Objects.hashCode(this.val);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ColoredTransition other = (ColoredTransition) obj;
        if (!Objects.equals(this.transition, other.transition)) {
            return false;
        }
        if (!Objects.equals(this.val, other.val)) {
            return false;
        }
        return true;
    }

    public HLPetriGame getHlgame() {
        return hlgame;
    }

    @Override
    public String toString() {
        return transition.getId() + "." + val.toString();
    }

}
