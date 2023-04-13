package uk.ac.ox.oxfish.model.regs.fads;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class DelLicenseRegulationFactory implements AlgorithmFactory<DelLicenseRegulation> {
    @Override
    public DelLicenseRegulation apply(final FishState fishState) {
        return new DelLicenseRegulation();
    }
}
