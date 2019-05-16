package uniolunisaar.adam.ds.highlevel.oneenv;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import uniol.apt.adt.IGraph;
import uniol.apt.adt.IGraphListener;
import uniol.apt.adt.pn.Flow;
import uniol.apt.adt.pn.Node;
import uniol.apt.adt.pn.PetriNet;
import uniol.apt.adt.pn.Place;
import uniol.apt.adt.pn.Transition;
import uniolunisaar.adam.ds.highlevel.ColoredTransition;
import uniolunisaar.adam.ds.highlevel.HLPetriGame;
import uniolunisaar.adam.ds.highlevel.Valuation;
import uniolunisaar.adam.ds.highlevel.ValuationIterator;

/**
 *
 * @author Manuel Gieseking
 */
public class OneEnvHLPG extends HLPetriGame implements IGraphListener<PetriNet, Flow, Node> {

    private List<Transition> sysTransitions = null;
    private List<ColoredTransition> singlePresetTransitions = null;

    public OneEnvHLPG(HLPetriGame game, boolean byReference) {
        super(game, byReference);
        addListener(this);
    }

    public OneEnvHLPG(String name) {
        super(name);
        addListener(this);
    }

    private boolean isSystem(Transition t) {
        for (Place place : t.getPreset()) {
            if (isEnvironment(place)) {
                return false;
            }
        }
        return true;
    }

    public Collection<Transition> getSystemTransitions() {
        if (sysTransitions == null) {
            sysTransitions = new ArrayList<>();
            for (Transition transition : getTransitions()) {
                if (isSystem(transition)) {
                    sysTransitions.add(transition);
                }
            }
        }
        return Collections.unmodifiableCollection(sysTransitions);
    }

    public Collection<ColoredTransition> getSinglePresetTransitions() {
        if (singlePresetTransitions == null) {
            singlePresetTransitions = new ArrayList<>();
            for (Transition transition : getTransitions()) {
                for (ValuationIterator it = this.getValuations(transition).iterator(); it.hasNext();) {
                    Valuation val = it.next();
                    ColoredTransition ct = new ColoredTransition(this, transition, val);
                    if (ct.isValid() && isSystem(transition) && ct.getPreset().size() == 1) {
                        singlePresetTransitions.add(ct);
                    }
                }
            }
        }
        return Collections.unmodifiableCollection(singlePresetTransitions);
    }

    @Override
    public boolean changeOccurred(IGraph<PetriNet, Flow, Node> graph) {
        sysTransitions = null;
        singlePresetTransitions = null;
        return true;
    }

}
