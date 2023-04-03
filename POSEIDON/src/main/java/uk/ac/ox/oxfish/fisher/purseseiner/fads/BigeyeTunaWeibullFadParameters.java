package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityEnvironmentalAttractorFactory.SpeciesParameters;
import uk.ac.ox.oxfish.parameters.FreeParameter;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;

public class BigeyeTunaWeibullFadParameters extends SpeciesParameters {
    public BigeyeTunaWeibullFadParameters() {
        super(
            new CalibratedParameter() {
                @Override
                @FreeParameter
                public void setFixedValue(final double fixedValue) {
                    super.setFixedValue(fixedValue);
                }
            },
            new CalibratedParameter() {
                @Override
                @FreeParameter
                public void setFixedValue(final double fixedValue) {
                    super.setFixedValue(fixedValue);
                }
            },
            new CalibratedParameter() {
                @Override
                @FreeParameter
                public void setFixedValue(final double fixedValue) {
                    super.setFixedValue(fixedValue);
                }
            }
        );
    }
}
