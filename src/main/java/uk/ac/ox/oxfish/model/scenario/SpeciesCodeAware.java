package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.SpeciesCodes;

public interface SpeciesCodeAware {
    SpeciesCodes getSpeciesCodes();

    void setSpeciesCodes(SpeciesCodes speciesCodes);
}
