package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.event.BiomassDrivenTimeSeriesExogenousCatchesFactory;
import uk.ac.ox.oxfish.model.scenario.InputPath;

public class BiomassProcessesFactory extends BiologicalProcessesFactory<BiomassLocalBiology> {
    private BiomassReallocatorFactory biomassReallocatorFactory;
    private BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory;
    private BiomassInitializerFactory biomassInitializerFactory;
    private BiomassRestorerFactory biomassRestorerFactory;
    private ScheduledBiomassProcessesFactory scheduledBiomassProcessesFactory;
    private int targetYear;

    @SuppressWarnings("unused")
    public BiomassProcessesFactory() {
        super();
    }

    public BiomassProcessesFactory(
        final InputPath inputFolder,
        final SpeciesCodesFromFileFactory speciesCodesSupplier,
        final int targetYear
    ) {
        super(inputFolder, speciesCodesSupplier);
        this.biomassReallocatorFactory =
            new BiomassReallocatorFactory(
                inputFolder.path("biomass_distributions.csv"),
                365,
                speciesCodesSupplier
            );
        this.exogenousCatchesFactory =
            new BiomassDrivenTimeSeriesExogenousCatchesFactory(
                speciesCodesSupplier,
                inputFolder.path("exogenous_catches.csv"),
                targetYear,
                true
            );
        this.biomassInitializerFactory =
            new BiomassInitializerFactory(
                speciesCodesSupplier,
                inputFolder.path("schaefer_params.csv")
            );
        this.biomassRestorerFactory = new BiomassRestorerFactory();
        this.scheduledBiomassProcessesFactory = new ScheduledBiomassProcessesFactory();
    }

    /**
     * @param nauticalMap The nautical map, which must be passed separately from the FishState, because at the time
     *                    this is called, the FishState's map has not been set.
     * @param fishState   The model.
     * @return The biology initializer and a list of startables.
     */
    public Processes init(
        final NauticalMap nauticalMap,
        final FishState fishState
    ) {
        biomassReallocatorFactory.setMapExtent(nauticalMap.getMapExtent());
        final BiomassReallocator biomassReallocator = biomassReallocatorFactory.apply(fishState);
        scheduledBiomassProcessesFactory.setBiomassReallocator(biomassReallocator);

        biomassRestorerFactory.setBiomassReallocator(biomassReallocator);
        biomassInitializerFactory.setBiomassReallocator(biomassReallocator);

        return new Processes(
            biomassInitializerFactory.apply(fishState),
            ImmutableList.of(
                scheduledBiomassProcessesFactory,
                biomassRestorerFactory,
                exogenousCatchesFactory
            )
        );
    }

    public BiomassInitializerFactory getBiomassInitializerFactory() {
        return biomassInitializerFactory;
    }

    public void setBiomassInitializerFactory(final BiomassInitializerFactory biomassInitializerFactory) {
        this.biomassInitializerFactory = biomassInitializerFactory;
    }

    public BiomassRestorerFactory getBiomassRestorerFactory() {
        return biomassRestorerFactory;
    }

    public void setBiomassRestorerFactory(final BiomassRestorerFactory biomassRestorerFactory) {
        this.biomassRestorerFactory = biomassRestorerFactory;
    }

    public ScheduledBiomassProcessesFactory getScheduledBiomassProcessesFactory() {
        return scheduledBiomassProcessesFactory;
    }

    public void setScheduledBiomassProcessesFactory(final ScheduledBiomassProcessesFactory scheduledBiomassProcessesFactory) {
        this.scheduledBiomassProcessesFactory = scheduledBiomassProcessesFactory;
    }

    public BiomassDrivenTimeSeriesExogenousCatchesFactory getExogenousCatchesFactory() {
        return exogenousCatchesFactory;
    }

    public void setExogenousCatchesFactory(final BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory) {
        this.exogenousCatchesFactory = exogenousCatchesFactory;
    }

    public BiomassReallocatorFactory getBiomassReallocatorFactory() {
        return biomassReallocatorFactory;
    }

    public void setBiomassReallocatorFactory(final BiomassReallocatorFactory biomassReallocatorFactory) {
        this.biomassReallocatorFactory = biomassReallocatorFactory;
    }

    public int getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final int targetYear) {
        this.targetYear = targetYear;
    }
}
