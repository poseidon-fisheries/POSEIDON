package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityEnvironmentalAttractorFactory.SpeciesParameters;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;

public class BigeyeTunaWeibullFadParameters extends SpeciesParameters {
    public BigeyeTunaWeibullFadParameters() {
        super(
            new CalibratedParameter(0.0001, 1.69958, 0.0001, 2.0, 1.0E-4),
            new CalibratedParameter(1200, 11000, 16375),
            new CalibratedParameter(0.03, 0.25, 0, 1, 0.16)
        );
    }
}
