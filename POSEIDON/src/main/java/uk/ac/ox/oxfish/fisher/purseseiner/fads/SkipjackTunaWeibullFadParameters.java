package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityEnvironmentalAttractorFactory.SpeciesParameters;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;

public class SkipjackTunaWeibullFadParameters extends SpeciesParameters {
    public SkipjackTunaWeibullFadParameters() {
        super(
            new CalibratedParameter(0.65, 3, 0.0001, 3, 2),
            new CalibratedParameter(16500, 1E5, 43382),
            new CalibratedParameter(0.005, 0.1, 0.075)
        );
    }
}
