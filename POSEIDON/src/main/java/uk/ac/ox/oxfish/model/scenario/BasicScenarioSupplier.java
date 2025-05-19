/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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

package uk.ac.ox.oxfish.model.scenario;

public class BasicScenarioSupplier implements ScenarioSupplier {

    private final Class<? extends Scenario> scenarioClass;
    private final String scenarioName;

    private final String scenarioDescription;

    public BasicScenarioSupplier(
        final Class<? extends Scenario> scenarioClass,
        final String scenarioName,
        final String scenarioDescription
    ) {
        this.scenarioClass = scenarioClass;
        this.scenarioName = scenarioName;
        this.scenarioDescription = scenarioDescription;
    }

    @Override
    public String getScenarioName() {
        return scenarioName;
    }

    @Override
    public Scenario get() {
        try {
            return scenarioClass.newInstance();
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getScenarioDescription() {
        return scenarioDescription;
    }
}
