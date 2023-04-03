package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EpoPurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.*;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.ValuePerSetFadModuleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesSupplier;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.fads.FadZapper;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.ChlorophyllMapFactory;
import uk.ac.ox.oxfish.model.plugins.FrontalIndexMapFactory;
import uk.ac.ox.oxfish.model.plugins.TemperatureMapFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.List;
import java.util.function.Predicate;

public class EpoPathPlanningAbundanceScenario extends EpoAbundanceScenario {

    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFromFileFactory(
            getInputFolder().path("abundance", "selectivity.csv"),
            getSpeciesCodesSupplier()
        );

    private PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> purseSeinerFleetFactory =
        new EpoPurseSeinerFleetFactory<>(
            getTargetYear(),
            getInputFolder(),
            getSpeciesCodesSupplier(),
            new AbundancePurseSeineGearFactory(
                new WeibullCatchabilitySelectivityEnvironmentalAttractorFactory(
                    getAbundanceFiltersFactory(),
                    ImmutableMap.of(
                        "Skipjack tuna", new SkipjackTunaWeibullFadParameters(),
                        "Bigeye tuna", new BigeyeTunaWeibullFadParameters(),
                        "Yellowfin tuna", new YellowfinTunaWeibullFadParameters()
                    ),
                    ImmutableList.of(
                        new ChlorophyllMapFactory(
                            getInputFolder().path("environmental_maps", "chlorophyll.csv")
                        ),
                        new TemperatureMapFactory(
                            getInputFolder().path("environmental_maps", "temperature.csv")
                        ),
                        new FrontalIndexMapFactory(
                            getInputFolder().path("environmental_maps", "frontal_index.csv")
                        )
                    ),
                    new FixedDoubleParameter(0.0014337500000000001),
                    new FixedDoubleParameter(5),
                    new FixedDoubleParameter(41.6127216390614),
                    new FixedDoubleParameter(3.401799402857515)
                )
            ),
            new EPOPlannedStrategyFlexibleFactory(
                getTargetYear(),
                new LocationValuesSupplier(
                    getInputFolder().path("location_values.csv"),
                    getTargetYear()
                ),
                new ValuePerSetFadModuleFactory(),
                new AbundanceCatchSamplersFactory(
                    getSpeciesCodesSupplier(),
                    getAbundanceFiltersFactory(),
                    getInputFolder().path("set_samples.csv")
                ),
                getInputFolder().path("action_weights.csv"),
                getInputFolder().path("boats.csv")
            ),
            new DefaultToDestinationStrategyFishingStrategyFactory()
        );
    private boolean zapper = false;
    private boolean zapperAge = false;

    public PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> getPurseSeinerFleetFactory() {
        return purseSeinerFleetFactory;
    }

    @SuppressWarnings("unused")
    public void setPurseSeinerFleetFactory(final PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> purseSeinerFleetFactory) {
        this.purseSeinerFleetFactory = purseSeinerFleetFactory;
    }

    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        purseSeinerFleetFactory.useDummyData(testFolder());
    }

    @Override
    public ScenarioPopulation populateModel(final FishState fishState) {
        final ScenarioPopulation scenarioPopulation = super.populateModel(fishState);
        if (zapper) {
            final Predicate<AbstractFad> predicate = zapperAge ?
                fad -> fad.getLocation().getGridX() <= 20 :
                fad -> fad.getLocation().getGridX() <= 20 || fishState.getStep() - fad.getStepDeployed() > 150;
            fishState.registerStartable(new FadZapper(predicate));
        }
        return scenarioPopulation;
    }

    @Override
    List<Fisher> makeFishers(final FishState fishState, final int targetYear) {
        return purseSeinerFleetFactory.makeFishers(fishState, targetYear);
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


