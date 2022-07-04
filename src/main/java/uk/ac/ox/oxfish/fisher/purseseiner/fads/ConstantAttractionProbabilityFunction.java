package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;

public class ConstantAttractionProbabilityFunction<B extends LocalBiology, F extends Fad<B, F>>
    implements AttractionProbabilityFunction<B, F> {

    private final double attractionProbability;

    public ConstantAttractionProbabilityFunction(final double attractionProbability) {
        this.attractionProbability = attractionProbability;
    }

    @Override
    public double apply(
        final Species species,
        final B biology,
        final Fad<B, F> fad
    ) {
        return attractionProbability;
    }
}
