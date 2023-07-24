package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.tuna.BiomassProcessesFactory;
import uk.ac.ox.oxfish.geography.fads.BiomassFadMapFactory;

public class EpoBiomassScenario extends EpoScenario<BiomassLocalBiology> {
    public EpoBiomassScenario() {
        setBiologicalProcesses(
            BiomassProcessesFactory.create(
                getInputFolder().path("biomass"),
                new SpeciesCodesFromFileFactory(
                    getInputFolder().path("species_codes.csv")
                ),
                getTargetYear()
            )
        );
        setFadMap(new BiomassFadMapFactory(getCurrentPatternMapSupplier()));
    }
}
