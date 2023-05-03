/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.BiomassPurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.EpoPurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.PurseSeinerFleetFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.BiomassCatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionFieldsSupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.LocationValuesSupplier;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerBiomassFishingStrategyFactory;
import uk.ac.ox.oxfish.geography.fads.CompressedBiomassFadInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;

/**
 * The biomass-based IATTC tuna simulation scenario.
 */
public class EpoGravityBiomassScenario extends EpoBiomassScenario {

    private PurseSeinerFleetFactory purseSeinerFleetFactory =
        new EpoPurseSeinerFleetFactory<>(
            getTargetYear(),
            getInputFolder(),
            getSpeciesCodesSupplier(),
            new BiomassPurseSeineGearFactory(
                new CompressedBiomassFadInitializerFactory(
                    getSpeciesCodesSupplier(),
                    // use numbers from https://github.com/poseidon-fisheries/tuna/blob/9c6f775ced85179ec39e12d8a0818bfcc2fbc83f/calibration/results/ernesto/best_base_line/calibrated_scenario.yaml
                    ImmutableMap.of(
                        "Bigeye tuna", 0.7697766896339598,
                        "Yellowfin tuna", 1.1292389959739901,
                        "Skipjack tuna", 0.0
                    ),
                    ImmutableMap.of(
                        "Bigeye tuna", 1.0184011081061861,
                        "Yellowfin tuna", 0.0,
                        "Skipjack tuna", 0.7138646301498129
                    ),
                    ImmutableMap.of(
                        "Bigeye tuna", 9.557509707646096,
                        "Yellowfin tuna", 10.419783885948643,
                        "Skipjack tuna", 9.492481930328207
                    ),
                    ImmutableMap.of(
                        "Bigeye tuna", 0.688914118975473,
                        "Yellowfin tuna", 0.30133562299610883,
                        "Skipjack tuna", 1.25
                    )
                )
            ),
            new GravityDestinationStrategyFactory(
                getTargetYear(),
                getInputFolder().path("action_weights.csv"),
                getInputFolder().path("vessels.csv"),
                new AttractionFieldsSupplier(
                    new LocationValuesSupplier(
                        getInputFolder().path("location_values.csv"),
                        getTargetYear()
                    ),
                    getInputFolder().path("max_current_speeds.csv")
                )
            ),
            new PurseSeinerBiomassFishingStrategyFactory(
                getTargetYear(),
                getSpeciesCodesSupplier(),
                getInputFolder().path("action_weights.csv"),
                new BiomassCatchSamplersFactory(
                    getSpeciesCodesSupplier(),
                    getInputFolder().path("set_samples.csv")
                ),
                new SetDurationSamplersFactory(getInputFolder().path("set_durations.csv")),
                getInputFolder().path("max_current_speeds.csv"),
                getInputFolder().path("set_compositions.csv")
            )
        );

    public PurseSeinerFleetFactory getPurseSeinerFleetFactory() {
        return purseSeinerFleetFactory;
    }

    @SuppressWarnings("unused")
    public void setPurseSeinerFleetFactory(final PurseSeinerFleetFactory purseSeinerFleetFactory) {
        this.purseSeinerFleetFactory = purseSeinerFleetFactory;
    }

    @Override
    List<Fisher> makeFishers(final FishState fishState, final int targetYear) {
        return purseSeinerFleetFactory.makeFishers(fishState, targetYear);
    }

    @Override
    public void useDummyData() {
        super.useDummyData();
        purseSeinerFleetFactory.useDummyData(testFolder());
    }

}
