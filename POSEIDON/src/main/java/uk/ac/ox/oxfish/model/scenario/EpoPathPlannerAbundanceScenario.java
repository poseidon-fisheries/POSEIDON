package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.AbundancePurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EpoPurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.SelectivityAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.WeibullPerSpeciesCarryingCapacitiesFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.EPOPlannedStrategyFlexibleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.MinimumSetValuesFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.planner.factories.ValuePerSetPlanningModuleFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.FixedLocationValuesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogNormalErrorOperatorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.UnreliableFishValueCalculatorFactory;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.DefaultToDestinationStrategyFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPenaltyFunctionFactory;
import uk.ac.ox.oxfish.model.plugins.FrontalIndexMapFactory;
import uk.ac.ox.oxfish.model.plugins.TemperatureMapFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedParameterTableFromFile;

public class EpoPathPlannerAbundanceScenario extends EpoAbundanceScenario {

    private AbundanceFiltersFactory abundanceFilters =
        new AbundanceFiltersFromFileFactory(
            getInputFolder().path("abundance", "selectivity_2022.csv")
        );

    public EpoPathPlannerAbundanceScenario() {
        final MinimumSetValuesFromFileFactory minimumSetValues =
            new MinimumSetValuesFromFileFactory(
                getInputFolder().path("min_set_values.csv")
            );
        setFleet(
            new EpoPurseSeinerFleetFactory(
                getTargetYear(),
                getInputFolder(),
                new AbundancePurseSeineGearFactory(
                    getTargetYear(),
                    new SelectivityAbundanceFadInitializerFactory(

                        // see https://github.com/poseidon-fisheries/tuna-issues/issues/141#issuecomment-1545974455
                        // for Weibull parameter values, obtained by fitting the distributions to observer data
                        new WeibullPerSpeciesCarryingCapacitiesFromFileFactory(
                            getInputFolder().path("fad_carrying_capacity_parameters.csv"),
                            getTargetYear(),
                            new CalibratedParameter(
                                0.75, 1.5, 0.5, 5, 1
                            ),
                            new CalibratedParameter(
                                0.9, 1, 0.75, 1
                            )
                        ),
                        getAbundanceFilters(),
                        new CalibratedParameter(
                            13, 30, 5, 40, 14
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new CalibratedParameter(0, 0.3, 0, 1),
                            "Skipjack tuna", new CalibratedParameter(0, 0.3, 0, 1),
                            "Yellowfin tuna", new CalibratedParameter(0, 0.3, 0, 1)
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new CalibratedParameter(0.08, 0.348, 0, 0.75),
                            "Skipjack tuna", new CalibratedParameter(0.247, 0.75, 0, 0.75),
                            "Yellowfin tuna", new CalibratedParameter(0.109, 0.226, 0, 0.75)
                        ),
                        new EnvironmentalPenaltyFunctionFactory(
                            ImmutableMap.of(
                                "Temperature", new TemperatureMapFactory(
                                    getInputFolder().path("environmental_maps", "temperature_2021_to_2023.csv"),
                                    365 * 3
                                ),
                                "FrontalIndex", new FrontalIndexMapFactory(
                                    getInputFolder().path("environmental_maps", "frontal_index_2021_to_2023.csv"),
                                    365 * 3
                                )
                            )
                        )
                    ),
                    // ref: https://github.com/poseidon-fisheries/tuna-issues/issues/141#issuecomment-1549923263
                    // For fixed parameter values see:
                    // https://github.com/poseidon-fisheries/tuna-issues/issues/202#issue-1779551927
                    new UnreliableFishValueCalculatorFactory(new LogNormalErrorOperatorFactory(
                        new FixedDoubleParameter(-0.14452),
                        new FixedDoubleParameter(0.14097)
                    )),
                    new FixedParameterTableFromFile(getInputFolder().path("other_parameters.csv"))
                ),
                new EPOPlannedStrategyFlexibleFactory(
                    getTargetYear(),
                    new FixedLocationValuesFactory(
                        getInputFolder().path("location_values.csv"),
                        getTargetYear()
                    ),
                    minimumSetValues,
                    new ValuePerSetPlanningModuleFactory(
                        minimumSetValues,
                        getTargetYear(),
                        new SquaresMapDiscretizerFactory(),
                        new CalibratedParameter(0, 1, 0, 1)
                    ),
                    new AbundanceCatchSamplersFactory(
                        getAbundanceFilters(),
                        getInputFolder().path("set_samples.csv"),
                        getTargetYear()
                    ),
                    getInputFolder().path("action_weights.csv"),
                    getInputFolder().path("vessels.csv")
                ),
                new DefaultToDestinationStrategyFishingStrategyFactory()
            )
        );
    }

    @SuppressWarnings("WeakerAccess")
    public AbundanceFiltersFactory getAbundanceFilters() {
        return abundanceFilters;
    }

    @SuppressWarnings("unused")
    public void setAbundanceFilters(final AbundanceFiltersFactory abundanceFilters) {
        this.abundanceFilters = abundanceFilters;
    }

}
