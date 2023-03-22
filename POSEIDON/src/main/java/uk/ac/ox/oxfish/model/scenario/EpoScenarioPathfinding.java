package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.tuna.AbundanceProcessesFactory;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.experiments.tuna.Runner;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeineVesselReader;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.departing.PurseSeinerDepartingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesSupplier;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.fads.*;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Predicate;

public class EpoScenarioPathfinding extends EpoScenario<AbundanceLocalBiology, AbundanceFad> {

    private DefaultToDestinationStrategyFishingStrategyFactory fishingStrategyFactory =
        new DefaultToDestinationStrategyFishingStrategyFactory();
    private AlgorithmFactory<? extends FadInitializer> fadInitializerFactory =
        new LastMomentAbundanceFadInitalizerFactory();

    private boolean zapper = false;
    private boolean zapperAge = false;

    // private boolean galapagosZapper = false;
    private EPOPlannedStrategyFlexibleFactory destinationStrategy =
        new EPOPlannedStrategyFlexibleFactory(
            new LocationValuesSupplier(
                getInputFolder().path("location_values.csv")
            ),
            new AbundanceCatchSamplersFactory(
                getSpeciesCodesSupplier(),
                new AbundanceFiltersFactory(
                    getInputFolder().path("abundance", "selectivity.csv"),
                    getSpeciesCodesSupplier()
                ),
                getInputFolder().path("set_samples.csv")
            ),
            getInputFolder().path("action_weights.csv"),
            getVesselsFile()
        );

    public EpoScenarioPathfinding() {
        setBiologicalProcessesFactory(
            new AbundanceProcessesFactory(getInputFolder().path("abundance"), getSpeciesCodesSupplier())
        );
        setFadMapFactory(new AbundanceFadMapFactory(getCurrentPatternMapSupplier()));
        setPurseSeineGearFactory(new AbundancePurseSeineGearFactory());
    }


    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends FadInitializer> getFadInitializerFactory() {
        return fadInitializerFactory;
    }

    @SuppressWarnings("unused")
    public void setFadInitializerFactory(final AlgorithmFactory<? extends FadInitializer> fadInitializerFactory) {
        this.fadInitializerFactory = fadInitializerFactory;
    }

    public DefaultToDestinationStrategyFishingStrategyFactory getFishingStrategyFactory() {
        return fishingStrategyFactory;
    }

    public void setFishingStrategyFactory(final DefaultToDestinationStrategyFishingStrategyFactory fishingStrategyFactory) {
        this.fishingStrategyFactory = fishingStrategyFactory;
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        getDestinationStrategy()
            .getLocationValuesSupplier()
            .setLocationValuesFile(testFolder().path("dummy_location_values.csv"));
        getDestinationStrategy().setActionWeightsFile(
            testFolder().path("dummy_action_weights.csv")
        );
        getDestinationStrategy().setMaxTripDurationFile(
            testFolder().path("dummy_boats.csv")
        );
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {
        super.setFishingStrategyFactory(fishingStrategyFactory);
        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);

        if (fadInitializerFactory instanceof AbstractAbundanceFadInitializerFactory)
            ((AbstractAbundanceFadInitializerFactory) fadInitializerFactory).setSpeciesCodesSupplier(speciesCodesSupplier);
        ((PluggableSelectivity) fadInitializerFactory).setSelectivityFilters(
            ((AbundanceCatchSamplersFactory) getDestinationStrategy().getCatchSamplersFactory())
                .getAbundanceFiltersFactory()
                .apply(fishState)
                .get(FadSetAction.class)
        );

        getPurseSeineGearFactory().setFadInitializerFactory(fadInitializerFactory);

        final FisherFactory fisherFactory = makeFisherFactory(
            fishState,
            getRegulationsFactory(),
            getPurseSeineGearFactory(),
            destinationStrategy,
            fishingStrategyFactory,
            new PurseSeinerDepartingStrategyFactory(false)
        );

        final List<Fisher> fishers =
            new PurseSeineVesselReader(
                getVesselsFile().get(),
                TARGET_YEAR,
                fisherFactory,
                buildPorts(fishState)
            ).
                apply(fishState);

        if (zapper) {
            final Predicate<AbstractFad> predicate = zapperAge ?
                fad -> fad.getLocation().getGridX() <= 20 :
                fad -> fad.getLocation().getGridX() <= 20 || fishState.getStep() - fad.getStepDeployed() > 150;
            fishState.registerStartable(
                new FadZapper(
                    predicate
                )
            );
        }
        scenarioPopulation.getPopulation().addAll(fishers);
        return scenarioPopulation;
    }

    public EPOPlannedStrategyFlexibleFactory getDestinationStrategy() {
        return destinationStrategy;
    }

    public void setDestinationStrategy(final EPOPlannedStrategyFlexibleFactory destinationStrategy) {
        this.destinationStrategy = destinationStrategy;
    }

    public boolean isZapper() {
        return zapper;
    }

    public void setZapper(final boolean zapper) {
        this.zapper = zapper;
    }

    public boolean isZapperAge() {
        return zapperAge;
    }

    public void setZapperAge(final boolean zapperAge) {
        this.zapperAge = zapperAge;
    }

}


