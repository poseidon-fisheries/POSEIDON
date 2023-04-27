package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EpoPurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.SelectivityAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullPerSpeciesCarryingCapacitiesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.ValuePerSetPlanningModuleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesSupplier;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPenaltyFunctionFactory;
import uk.ac.ox.oxfish.model.plugins.FrontalIndexMapFactory;
import uk.ac.ox.oxfish.model.plugins.TemperatureMapFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;

import java.util.List;

public class EpoPathPlannerAbundanceScenario extends EpoAbundanceScenario {

    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFromFileFactory(
            getInputFolder().path("abundance", "selectivity.csv"),
            getSpeciesCodesSupplier()
        );

    private PurseSeinerFleetFactory purseSeinerFleetFactory =
        new EpoPurseSeinerFleetFactory<>(
            getTargetYear(),
            getInputFolder(),
            getSpeciesCodesSupplier(),
            new AbundancePurseSeineGearFactory(
                new SelectivityAbundanceFadInitializerFactory(
                    new WeibullPerSpeciesCarryingCapacitiesFactory(
                        ImmutableMap.of(
                            "Bigeye tuna", new CalibratedParameter(0.0001, 1.69958, 0.0001, 2.0, 1.0E-4),
                            "Skipjack tuna", new CalibratedParameter(0.65, 3, 0.0001, 3, 2),
                            "Yellowfin tuna", new CalibratedParameter(0.08, 3.75, 0.0001, 4, 2)
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new CalibratedParameter(1200, 11000, 16375),
                            "Skipjack tuna", new CalibratedParameter(16500, 1E5, 43382),
                            "Yellowfin tuna", new CalibratedParameter(5000, 100000, 71500)
                        )
                    ),
                    getAbundanceFiltersFactory(),
                    ImmutableMap.of(
                        "Bigeye tuna", new CalibratedParameter(0.03, 0.25, 0, 1, 0.16),
                        "Skipjack tuna", new CalibratedParameter(0.005, 0.1, 0.075),
                        "Yellowfin tuna", new CalibratedParameter(0.008, 0.03, 0.02)
                    ),
                    new EnvironmentalPenaltyFunctionFactory(
                        ImmutableMap.of(
                            "Temperature", new TemperatureMapFactory(
                                getInputFolder().path("environmental_maps", "temperature.csv")
                            ),
                            "FrontalIndex", new FrontalIndexMapFactory(
                                getInputFolder().path("environmental_maps", "frontal_index.csv")
                            )
                        )
                    )
                )
            ),
            new EPOPlannedStrategyFlexibleFactory(
                getTargetYear(),
                new LocationValuesSupplier(
                    getInputFolder().path("location_values.csv"),
                    getTargetYear()
                ),
                new ValuePerSetPlanningModuleFactory(),
                new AbundanceCatchSamplersFactory(
                    getSpeciesCodesSupplier(),
                    getAbundanceFiltersFactory(),
                    getInputFolder().path("set_samples.csv")
                ),
                getInputFolder().path("action_weights.csv"),
                getInputFolder().path("vessels.csv")
            ),
            new DefaultToDestinationStrategyFishingStrategyFactory()
        );

    public PurseSeinerFleetFactory getPurseSeinerFleetFactory() {
        return purseSeinerFleetFactory;
    }

    @SuppressWarnings("unused")
    public void setPurseSeinerFleetFactory(final PurseSeinerFleetFactory purseSeinerFleetFactory) {
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
    List<Fisher> makeFishers(final FishState fishState, final int targetYear) {
        return purseSeinerFleetFactory.makeFishers(fishState, targetYear);
    }

}


