package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EpoPurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.SelectivityAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullPerSpeciesCarryingCapacitiesFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.ValuePerSetPlanningModuleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogNormalErrorOperatorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.UnreliableFishValueCalculatorFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPenaltyFunctionFactory;
import uk.ac.ox.oxfish.model.plugins.FrontalIndexMapFactory;
import uk.ac.ox.oxfish.model.plugins.TemperatureMapFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class EpoPathPlannerAbundanceScenario extends EpoAbundanceScenario {

    private AbundanceFiltersFactory abundanceFilters = new AbundanceFiltersFromFileFactory(getInputFolder().path(
        "abundance",
        "selectivity_2022.csv"
    ));

    public EpoPathPlannerAbundanceScenario() {
        setFleet(new EpoPurseSeinerFleetFactory(getTargetYear(), getInputFolder(), new AbundancePurseSeineGearFactory(
            new SelectivityAbundanceFadInitializerFactory(
                // see https://github.com/poseidon-fisheries/tuna-issues/issues/141#issuecomment-1545974455
                // for Weibull parameter values, obtained by fitting the distributions to observer data
                new WeibullPerSpeciesCarryingCapacitiesFromFileFactory(
                    getInputFolder().path(
                        "fad_carrying_capacity_parameters.csv"),
                    getTargetYear(),
                    new CalibratedParameter(1, 1.5, 0, 2, 1)
                ), getAbundanceFilters(), ImmutableMap.of(
                "Bigeye tuna",
                new CalibratedParameter(0.03, 0.25, 0, 1, 0.16),
                "Skipjack tuna",
                new CalibratedParameter(0.005, 0.25, 0, 1, 0.075),
                "Yellowfin tuna",
                new CalibratedParameter(0.008, 0.25, 0, 1, 0.02)
            ), new EnvironmentalPenaltyFunctionFactory(ImmutableMap.of(
                "Temperature",
                new TemperatureMapFactory(
                    getInputFolder().path("environmental_maps", "temperature_2021_to_2023.csv"),
                    365 * 3
                ),
                "FrontalIndex",
                new FrontalIndexMapFactory(getInputFolder().path(
                    "environmental_maps",
                    "frontal_index_2021_to_2023.csv"
                ), 365 * 3)
            ))),
            // ref: https://github.com/poseidon-fisheries/tuna-issues/issues/141#issuecomment-1549923263
            // For fixed parameter values see:
            // https://github.com/poseidon-fisheries/tuna-issues/issues/202#issue-1779551927
            new UnreliableFishValueCalculatorFactory(new LogNormalErrorOperatorFactory(
                new FixedDoubleParameter(-0.14452),
                new FixedDoubleParameter(0.14097)
            ))
        ), new EPOPlannedStrategyFlexibleFactory(
            getTargetYear(),
            new LocationValuesFactory(
                getInputFolder().path("location_values.csv"),
                new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                getTargetYear()
            ),
            new ValuePerSetPlanningModuleFactory(),
            new AbundanceCatchSamplersFactory(
                getAbundanceFilters(),
                getInputFolder().path("set_samples.csv"),
                getTargetYear()
            ),
            getInputFolder().path("action_weights.csv"),
            getInputFolder().path("vessels.csv")
        ), new DefaultToDestinationStrategyFishingStrategyFactory()));
    }

    @SuppressWarnings("WeakerAccess")
    public AbundanceFiltersFactory getAbundanceFilters() {
        return abundanceFilters;
    }

    public void setAbundanceFilters(final AbundanceFiltersFactory abundanceFilters) {
        this.abundanceFilters = abundanceFilters;
    }

}


