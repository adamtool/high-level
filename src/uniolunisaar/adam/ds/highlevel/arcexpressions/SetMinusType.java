package uniolunisaar.adam.ds.highlevel.arcexpressions;

import uniolunisaar.adam.ds.highlevel.terms.IColorClassType;
import uniolunisaar.adam.ds.highlevel.terms.IColorType;

/**
 *
 * @author Manuel Gieseking
 */
public class SetMinusType implements IArcType, IArcTupleElementType {

    private final IColorClassType colorClass;
    private final IColorType color;

    public SetMinusType(IColorClassType colorClass, IColorType color) {
        this.colorClass = colorClass;
        this.color = color;
    }

    public IColorClassType getColorClass() {
        return colorClass;
    }

    public IColorType getColor() {
        return color;
    }

}
