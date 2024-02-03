package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.event.BiomassDrivenTimeSeriesExogenousCatchesFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import static uk.ac.ox.oxfish.model.scenario.EpoScenario.DEFAULT_MAP_EXTENT_FACTORY;

public class BiomassProcessesFactory extends BiologicalProcessesFactory<BiomassLocalBiology> {
    private BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory;

    public BiomassProcessesFactory() {
    }

    public BiomassProcessesFactory(
        final InputPath inputFolder,
        final BiologyInitializerFactory<BiomassLocalBiology> biologyInitializer,
        final RestorerFactory<BiomassLocalBiology> restorer,
        final ScheduledBiologicalProcessesFactory<BiomassLocalBiology> scheduledProcesses,
        final BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory
    ) {
        super(inputFolder, biologyInitializer, restorer, scheduledProcesses);
        this.exogenousCatchesFactory = exogenousCatchesFactory;
    }

    public static BiomassProcessesFactory create(
        final InputPath inputFolder,
        final SpeciesCodesFromFileFactory speciesCodesSupplier,
        final IntegerParameter targetYear
    ) {
        final BiomassReallocatorFactory reallocator = new BiomassReallocatorFactory(
            inputFolder.path("biomass_distributions.csv"),
            new IntegerParameter(365),
            DEFAULT_MAP_EXTENT_FACTORY
        );
        return new BiomassProcessesFactory(
            inputFolder,
            new BiomassInitializerFactory(
                reallocator,
                speciesCodesSupplier,
                inputFolder.path("schaefer_params.csv")
            ),
            new BiomassRestorerFactory(
                reallocator,
                ImmutableMap.of(0, 364)
            ),
            new ScheduledBiomassProcessesFactory(reallocator),
            new BiomassDrivenTimeSeriesExogenousCatchesFactory(
                speciesCodesSupplier,
                inputFolder.path("exogenous_catches.csv"),
                targetYear,
                true
            )
        );
    }

    @Override
    public BiologicalProcesses apply(final FishState fishState) {
        final BiologicalProcesses biologicalProcesses = super.apply(fishState);
        return new BiologicalProcesses(
            biologicalProcesses.getBiologyInitializer(),
            biologicalProcesses.getGlobalBiology(),
            ImmutableList.<AlgorithmFactory<? extends Startable>>builder()
                .addAll(biologicalProcesses.getStartableFactories())
                .add(exogenousCatchesFactory)
                .build()
        );
    }

    public BiomassDrivenTimeSeriesExogenousCatchesFactory getExogenousCatchesFactory() {
        return exogenousCatchesFactory;
    }

    @SuppressWarnings("unused")
    public void setExogenousCatchesFactory(final BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory) {
        this.exogenousCatchesFactory = exogenousCatchesFactory;
    }
}
