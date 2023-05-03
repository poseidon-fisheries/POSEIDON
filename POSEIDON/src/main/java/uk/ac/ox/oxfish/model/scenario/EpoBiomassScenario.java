package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.BiomassProcessesFactory;
import uk.ac.ox.oxfish.geography.fads.BiomassFadMapFactory;

public class EpoBiomassScenario extends EpoScenario<BiomassLocalBiology> {
    public EpoBiomassScenario() {
        setBiologicalProcessesFactory(
            new BiomassProcessesFactory(
                getInputFolder().path("biomass"),
                getSpeciesCodesSupplier(),
                getTargetYear()
            )
        );
        setFadMapFactory(new BiomassFadMapFactory(getCurrentPatternMapSupplier()));
    }
}
