package uk.ac.ox.poseidon.regulations.core.conditions;

import com.vividsolutions.jts.geom.Envelope;
import uk.ac.ox.poseidon.agents.api.Action;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class InRectangularArea implements Condition {
    private final Envelope envelope;

    public InRectangularArea(final Envelope envelope) {
        this.envelope = envelope;
    }

    @Override
    public boolean test(final Action action) {
        return action
            .getCoordinate()
            .map(envelope::contains)
            .orElse(false);
    }
}
