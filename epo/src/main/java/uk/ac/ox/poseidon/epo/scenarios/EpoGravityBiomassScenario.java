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

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.BiomassCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionFieldsFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.CappedMutableLocationValuesFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerBiomassFishingStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogNormalErrorOperatorFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.UnreliableFishValueCalculatorFactory;
import uk.ac.ox.oxfish.geography.fads.CompressedBiomassFadInitializerFactory;
import uk.ac.ox.oxfish.utility.parameters.CalibratedParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedParameterTableFromFile;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.epo.fleet.BiomassPurseSeineGearFactory;
import uk.ac.ox.poseidon.epo.fleet.EpoPurseSeinerFleetFactory;
import uk.ac.ox.poseidon.geography.GridsByMonthDayFromFileFactory;

/**
 * The biomass-based IATTC tuna simulation scenario.
 */
public class EpoGravityBiomassScenario extends EpoBiomassScenario {

    public EpoGravityBiomassScenario() {
        setFleet(
            new EpoPurseSeinerFleetFactory(
                getTargetYear(),
                getInputFolder(),
                new BiomassPurseSeineGearFactory(
                    getTargetYear(),
                    new CompressedBiomassFadInitializerFactory(
                        new FixedDoubleParameter(445_000),
                        // use numbers from https://github.com/poseidon-fisheries/tuna/blob/9c6f775ced85179ec39e12d8a0818bfcc2fbc83f/calibration/results/ernesto/best_base_line/calibrated_scenario.yaml
                        ImmutableMap.of(
                            "Bigeye tuna", new FixedDoubleParameter(0.7697766896339598),
                            "Yellowfin tuna", new FixedDoubleParameter(1.1292389959739901),
                            "Skipjack tuna", new FixedDoubleParameter(0.0)
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new FixedDoubleParameter(1.0184011081061861),
                            "Yellowfin tuna", new FixedDoubleParameter(0.0),
                            "Skipjack tuna", new FixedDoubleParameter(0.7138646301498129)
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new FixedDoubleParameter(9.557509707646096),
                            "Yellowfin tuna", new FixedDoubleParameter(10.419783885948643),
                            "Skipjack tuna", new FixedDoubleParameter(9.492481930328207)
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new FixedDoubleParameter(0.688914118975473),
                            "Yellowfin tuna", new FixedDoubleParameter(0.30133562299610883),
                            "Skipjack tuna", new FixedDoubleParameter(1.25)
                        ),
                        ImmutableMap.of(
                            "Bigeye tuna", new CalibratedParameter(0.08, 0.348, 0, 0.75),
                            "Skipjack tuna", new CalibratedParameter(0.247, 0.75, 0, 0.75),
                            "Yellowfin tuna", new CalibratedParameter(0.109, 0.226, 0, 0.75)
                        )
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
                new PurseSeinerBiomassFishingStrategyFactory(
                    getTargetYear(),
                    getInputFolder().path("action_weights.csv"),
                    new BiomassCatchSamplersFactory(
                        getInputFolder().path("set_samples.csv"),
                        getTargetYear()
                    ),
                    new SetDurationSamplersFactory(getInputFolder().path("set_durations.csv")),
                    getInputFolder().path("max_current_speeds.csv"),
                    getInputFolder().path("set_compositions.csv")
                )
            )
        );
    }

}
