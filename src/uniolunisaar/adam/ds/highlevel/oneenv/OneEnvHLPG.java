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
import uniolunisaar.adam.ds.highlevel.HLPetriGame;

/**
 *
 * @author Manuel Gieseking
 */
public class OneEnvHLPG extends HLPetriGame implements IGraphListener<PetriNet, Flow, Node> {

    private List<Transition> sysTransitions = null;
    private List<Transition> singlePresetTransitions = null;

    public OneEnvHLPG(HLPetriGame game) {
        super(game, true);
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

    public Collection<Transition> getSinglePresetTransitions() {
        if (singlePresetTransitions == null) {
            singlePresetTransitions = new ArrayList<>();
            for (Transition transition : getTransitions()) {
                if (isSystem(transition) && transition.getPreset().size() == 1) {
                    singlePresetTransitions.add(transition);
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
