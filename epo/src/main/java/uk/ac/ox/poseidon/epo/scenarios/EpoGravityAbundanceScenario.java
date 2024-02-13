/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.poseidon.epo.scenarios;

import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFromFileFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionFieldsFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.CappedMutableLocationValuesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerAbundanceFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogNormalErrorOperatorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.UnreliableFishValueCalculatorFactory;
import uk.ac.ox.oxfish.geography.fads.LinearAbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedParameterTableFromFile;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.epo.fleet.AbundancePurseSeineGearFactory;
import uk.ac.ox.poseidon.epo.fleet.EpoPurseSeinerFleetFactory;
import uk.ac.ox.poseidon.geography.GridsByMonthDayFromFileFactory;

/**
 * An age-structured scenario for purse-seine fishing in the Eastern Pacific Ocean.
 */
public class EpoGravityAbundanceScenario extends EpoAbundanceScenario {

    private AbundanceFiltersFactory abundanceFiltersFactory =
        new AbundanceFiltersFromFileFactory(
            getInputFolder().path("abundance", "selectivity_2022.csv")
        );

    public EpoGravityAbundanceScenario() {
        super();
        setFleet(
            new EpoPurseSeinerFleetFactory(
                getTargetYear(),
                getInputFolder(),
                new AbundancePurseSeineGearFactory(
                    getTargetYear(),
                    new LinearAbundanceFadInitializerFactory(
                        getAbundanceFiltersFactory(),
                        new FixedDoubleParameter(445_000),
                        "Bigeye tuna", "Yellowfin tuna", "Skipjack tuna"
                    ),
                    new UnreliableFishValueCalculatorFactory(new LogNormalErrorOperatorFactory(
                        new CalibratedParameter(-.2, .2, -.4, .4),
                        new CalibratedParameter(.2, .3, .01, .5)
                    )),
                    new FixedParameterTableFromFile(getInputFolder().path("other_parameters.csv")),
                    new GridsByMonthDayFromFileFactory(
                        getInputFolder().path("currents", "shear_2022.csv"),
                        new StringParameter("date"),
                        new StringParameter("lon"),
                        new StringParameter("lat"),
                        new StringParameter("value"),
                        getMapExtentFactory()
                    )
                ),
                new GravityDestinationStrategyFactory(
                    getTargetYear(),
                    getInputFolder().path("action_weights.csv"),
                    getInputFolder().path("vessels.csv"),
                    new AttractionFieldsFactory(
                        new CappedMutableLocationValuesFactory(
                            getInputFolder().path("location_values.csv"),
                            new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                            new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                            new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                            new CalibratedParameter(0, 0.1, 0, 1, 0.01),
                            getTargetYear(),
                            new IntegerParameter(50)
                        ),
                        getInputFolder().path("max_current_speeds.csv"),
                        getTargetYear()
                    )
                ),
                new PurseSeinerAbundanceFishingStrategyFactory(
                    getTargetYear(),
                    getInputFolder().path("action_weights.csv"),
                    new AbundanceCatchSamplersFactory(
                        getAbundanceFiltersFactory(),
                        getInputFolder().path("set_samples.csv"),
                        getTargetYear()
                    ),
                    new SetDurationSamplersFactory(
                        getInputFolder().path("set_durations.csv")
                    ),
                    getInputFolder().path("max_current_speeds.csv"),
                    getInputFolder().path("set_compositions.csv")
                )
            )
        );
    }

    @SuppressWarnings("WeakerAccess")
    public AbundanceFiltersFactory getAbundanceFiltersFactory() {
        return abundanceFiltersFactory;
    }

    @SuppressWarnings("unused")
    public void setAbundanceFiltersFactory(final AbundanceFiltersFactory abundanceFiltersFactory) {
        this.abundanceFiltersFactory = abundanceFiltersFactory;
    }

}
