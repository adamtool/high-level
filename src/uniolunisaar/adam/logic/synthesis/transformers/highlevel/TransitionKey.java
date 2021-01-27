package uniolunisaar.adam.logic.synthesis.transformers.highlevel;

import java.util.Objects;
import uniolunisaar.adam.ds.synthesis.highlevel.Valuation;

/**
 *
 * @author Manuel Gieseking
 */
public class TransitionKey {

    private final String id;
    private final Valuation val;

    public TransitionKey(String id, Valuation val) {
        this.id = id;
        this.val = val;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.id);
        hash = 43 * hash + Objects.hashCode(this.val);
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
        final TransitionKey other = (TransitionKey) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.val, other.val)) {
            return false;
        }
        return true;
    }

}
