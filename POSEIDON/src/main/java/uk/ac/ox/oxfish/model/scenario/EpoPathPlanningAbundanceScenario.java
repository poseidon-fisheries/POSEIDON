package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EpoPurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullCatchabilitySelectivityEnvironmentalAttractorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesSupplier;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.fads.FadZapper;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
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
                        "Skipjack tuna", 2.0,
                        "Bigeye tuna", 1.0E-4,
                        "Yellowfin tuna", 2.0
                    ),
                    ImmutableMap.of(
                        "Skipjack tuna", 43382.94042870394,
                        "Bigeye tuna", 16374.74063889846,
                        "Yellowfin tuna", 71487.06619444962
                    ),
                    ImmutableMap.of(
                        "Skipjack tuna", 0.07370525744999998,
                        "Bigeye tuna", 0.16023563707903266,
                        "Yellowfin tuna", 0.0205577772
                    ),
                    new FixedDoubleParameter(0.0014337500000000001),
                    new FixedDoubleParameter(5),
                    new FixedDoubleParameter(41.6127216390614),
                    new FixedDoubleParameter(3.401799402857515),
                    ImmutableList.of(
                        new AdditionalMapFactory(
                            "Chlorophyll",
                            getInputFolder().path("environmental_maps", "chlorophyll.csv")
                        ),
                        new AdditionalMapFactory(
                            "Temperature",
                            getInputFolder().path("environmental_maps", "temperature.csv")
                        ),
                        new AdditionalMapFactory(
                            "FrontalIndex",
                            getInputFolder().path("environmental_maps", "frontal_index.csv")
                        )
                    ),
                    ImmutableList.of(
                        new FixedDoubleParameter(0.0938754550536813),
                        new FixedDoubleParameter(28.0084),
                        new FixedDoubleParameter(0)
                    ),
                    ImmutableList.of(
                        new FixedDoubleParameter(2),
                        new FixedDoubleParameter(2),
                        new FixedDoubleParameter(2)
                    )
                )
            ),
            new EPOPlannedStrategyFlexibleFactory(
                getTargetYear(),
                new LocationValuesSupplier(
                    getInputFolder().path("location_values.csv"),
                    getTargetYear()
                ),
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

    public PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> getPurseSeinerFleetFactory() {
        return purseSeinerFleetFactory;
    }

    @SuppressWarnings("unused")
    public void setPurseSeinerFleetFactory(final PurseSeinerFleetFactory<AbundanceLocalBiology, AbundanceFad> purseSeinerFleetFactory) {
        this.purseSeinerFleetFactory = purseSeinerFleetFactory;
    }

    private boolean zapper = false;
    private boolean zapperAge = false;

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


