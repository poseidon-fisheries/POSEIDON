package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.event.BiomassDrivenTimeSeriesExogenousCatchesFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class BiomassProcessesFactory extends BiologicalProcessesFactory<BiomassLocalBiology> {
    private BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory;

    @SuppressWarnings("unused")
    public BiomassProcessesFactory() {
        super();
    }

    public BiomassProcessesFactory(
        final InputPath inputFolder,
        final SpeciesCodesFromFileFactory speciesCodesSupplier,
        final int targetYear
    ) {
        super(
            inputFolder,
            new BiomassInitializerFactory(
                speciesCodesSupplier,
                inputFolder.path("schaefer_params.csv")
            ),
            new BiomassReallocatorFactory(
                inputFolder.path("biomass_distributions.csv"),
                365
            ),
            new BiomassRestorerFactory(),
            new ScheduledBiomassProcessesFactory()
        );
        this.exogenousCatchesFactory =
            new BiomassDrivenTimeSeriesExogenousCatchesFactory(
                speciesCodesSupplier,
                inputFolder.path("exogenous_catches.csv"),
                targetYear,
                true
            );
    }

    /**
     * @param nauticalMap The nautical map, which must be passed separately from the FishState, because at the time
     *                    this is called, the FishState's map has not been set.
     * @param fishState   The model.
     * @return The biology initializer and a list of startables.
     */
    @Override
    public Processes initProcesses(
        final NauticalMap nauticalMap,
        final FishState fishState
    ) {
        final Processes processes = super.initProcesses(nauticalMap, fishState);
        return new Processes(
            processes.biologyInitializer,
            processes.globalBiology,
            ImmutableList.<AlgorithmFactory<? extends Startable>>builder()
                .addAll(processes.startableFactories)
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
