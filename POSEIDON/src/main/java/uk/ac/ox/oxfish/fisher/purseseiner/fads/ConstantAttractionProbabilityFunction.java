package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;

public class ConstantAttractionProbabilityFunction
    implements AttractionProbabilityFunction {

    private final double attractionProbability;

    public ConstantAttractionProbabilityFunction(final double attractionProbability) {
        this.attractionProbability = attractionProbability;
    }

    @Override
    public double apply(
        final Species species,
        final LocalBiology biology,
        final AggregatingFad<?, ?, ?> fad
    ) {
        return attractionProbability;
    }

}
