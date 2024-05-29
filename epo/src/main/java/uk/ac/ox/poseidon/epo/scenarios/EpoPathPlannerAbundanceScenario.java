/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.poseidon.epo.scenarios;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FixedGlobalCarryingCapacitySupplierFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.SelectivityAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.environment.EnvironmentalPenaltyFactory;
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
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedParameterTableFromFileFactory;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.epo.fleet.AbundancePurseSeineGearFactory;
import uk.ac.ox.poseidon.epo.fleet.EpoPurseSeinerFleetFactory;
import uk.ac.ox.poseidon.geography.GridsByDateFromFileFactory;
import uk.ac.ox.poseidon.geography.GridsByMonthDayFromFileFactory;

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
                        new FixedGlobalCarryingCapacitySupplierFactory(
                            // 456t is the biggest set in observer data from 2017-2023
                            new FixedDoubleParameter(456_000)
                        ),
                        getAbundanceFilters(),
                        new CalibratedParameter(
                            13, 30, 5, 40, 14
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new CalibratedParameter(0.5, 0.9, 0, 1),
                            "Skipjack tuna", new CalibratedParameter(0, 0.5, 0, 1),
                            "Yellowfin tuna", new CalibratedParameter(0, 0.5, 0, 1)
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new CalibratedParameter(0.5, 0.75, 0, 0.85),
                            "Skipjack tuna", new CalibratedParameter(0.05, 0.75, 0, 0.75),
                            "Yellowfin tuna", new CalibratedParameter(0.109, 0.226, 0, 0.75)
                        ),
                        ImmutableMap.of(
                            "Temperature", new EnvironmentalPenaltyFactory(
                                new GridsByDateFromFileFactory(
                                    getInputFolder().path(
                                        "environmental_maps",
                                        "temperature_2021_to_2023.csv"
                                    ),
                                    getMapExtentFactory()
                                ),
                                new CalibratedParameter(26, 27, 25, 30, 26.5), // target
                                new CalibratedParameter(4, 6, 2, 8, 2.25), // margin
                                new CalibratedParameter(.25, .6, 0, 0.8) // penalty
                            ),
                            "FrontalIndex", new EnvironmentalPenaltyFactory(
                                new GridsByDateFromFileFactory(
                                    getInputFolder().path(
                                        "environmental_maps",
                                        "frontal_index_2021_to_2023.csv"
                                    ),
                                    getMapExtentFactory()
                                ),
                                new CalibratedParameter(0, 2, 0, 3), // target
                                new CalibratedParameter(0, 2, 0, 2), // margin
                                new CalibratedParameter(.25, .75, 0, 1) // penalty
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
                    new FixedParameterTableFromFileFactory(getInputFolder().path(
                        "other_parameters.csv")),
                    new GridsByMonthDayFromFileFactory(
                        getInputFolder().path("currents", "shear_2022.csv"),
                        getMapExtentFactory()
                    )
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
                        new SquaresMapDiscretizerFactory(
                            new CalibratedParameter(3, 10, 1, 50, 6),
                            new CalibratedParameter(3, 10, 1, 50, 3)
                        ),
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
