package uniolunisaar.adam.ds.synthesis.highlevel.arcexpressions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.IColorClassType;
import uniolunisaar.adam.ds.synthesis.highlevel.terms.IColorType;

/**
 *
 * @author Manuel Gieseking
 */
public class SetMinusType implements IArcType, IArcTupleElementType {

    private final IColorClassType colorClass;
    private final List<IColorType> colors;

    public SetMinusType(IColorClassType colorClass, IColorType... colors) {
        this.colorClass = colorClass;
        this.colors = new ArrayList<>(Arrays.asList(colors));
    }

    public IColorClassType getColorClass() {
        return colorClass;
    }

    public List<IColorType> getColors() {
        return colors;
    }

}
