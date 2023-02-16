/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.scenario;

import static uk.ac.ox.oxfish.geography.fads.ExogenousFadSetter.initFadRemovalLog;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.TESTS_INPUT_PATH;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import junit.framework.TestCase;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

public class FadsOnlyEpoAbundanceScenarioTest extends TestCase {

    public void testRunTwoYearsWithoutCrashing() {
        final FadsOnlyEpoAbundanceScenario scenario = new FadsOnlyEpoAbundanceScenario();
        scenario.useDummyData();
        final FishState fishState = new FishState();
        initFadRemovalLog();
        fishState.setScenario(scenario);
        fishState.start();
        do {
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 2);
    }

}