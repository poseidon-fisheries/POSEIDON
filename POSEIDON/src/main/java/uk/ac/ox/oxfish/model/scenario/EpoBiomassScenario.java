package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.BiomassProcessesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.geography.fads.BiomassFadMapFactory;

public class EpoBiomassScenario extends EpoScenario<BiomassLocalBiology, BiomassFad> {
    public EpoBiomassScenario() {
        setBiologicalProcessesFactory(
            new BiomassProcessesFactory(
                getInputFolder().path("biomass"),
                getSpeciesCodesSupplier(),
                TARGET_YEAR
            )
        );
        setFadMapFactory(new BiomassFadMapFactory(getCurrentPatternMapSupplier()));
    }
}
