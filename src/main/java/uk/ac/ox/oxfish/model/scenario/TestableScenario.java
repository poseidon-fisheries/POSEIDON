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

import static uk.ac.ox.oxfish.model.scenario.EpoScenario.TESTS_INPUT_PATH;

import java.nio.file.Path;
import uk.ac.ox.oxfish.model.FishState;

public interface TestableScenario extends Scenario {

    void useDummyData(final Path dummyDataPath);

    static FishState startTestableScenario(
        final Class<? extends TestableScenario> scenarioClass
    ) {
        final TestableScenario scenario;
        try {
            scenario = scenarioClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
        scenario.useDummyData(TESTS_INPUT_PATH);
        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        fishState.start();
        return fishState;
    }

}
