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

package uk.ac.ox.oxfish.biology.growers;

import static org.apache.logging.log4j.Level.DEBUG;
import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.utility.CsvLogger.addCsvLogger;

import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.BiomassPurseSeineGearFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination.GravityDestinationStrategyFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.PurseSeinerBiomassFishingStrategyFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.NoFishingFactory;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.model.scenario.FisherDefinition;
import uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario;

public class FadAwareLogisticGrowerTest {

    @Test
    public void jonLandings() {

        addCsvLogger(
            DEBUG,
            "biomass_events",
            "step,stepOrder,process,species,biomassBefore,biomassAfter"
        );

        final EpoBiomassScenario scenario = new EpoBiomassScenario();
        scenario.useDummyData(EpoScenario.INPUT_PATH.resolve("test"));

        scenario.getExogenousCatchesFactory()
            .setCatchesFile(Paths.get("inputs", "tests", "exogenous_catches.csv"));
        scenario.getFisherDefinition().setRegulation(new NoFishingFactory());

        final FishState state = new FishState();
        state.setScenario(scenario);
        state.start();

        while (state.getYear() < 5) {
            state.schedule.step(state);
        }
        state.schedule.step(state);

        final Species yft = state.getBiology().getSpecie("Yellowfin tuna");
        assertEquals(889195.40, state.getTotalBiomass(yft) / 1000.0, 10.0);


    }


}