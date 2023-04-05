package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityEnvironmentalAttractorFactory.SpeciesParameters;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;

public class YellowfinTunaWeibullFadParameters extends SpeciesParameters {
    public YellowfinTunaWeibullFadParameters() {
        super(
            new CalibratedParameter(0.08, 3.75, 0.0001, 4, 2),
            new CalibratedParameter(5000, 100000, 71500),
            new CalibratedParameter(0.008, 0.03, 0.02)
        );
    }
}
