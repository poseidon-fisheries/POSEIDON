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

package uk.ac.ox.oxfish.experiments.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class Policy<S extends Scenario> implements RowProvider {

    public static final Policy<Scenario> DEFAULT =
        new Policy<>(
            "Default",
            scenario -> {
            }
        );

    private final String name;
    private final Consumer<S> scenarioConsumer;

    public Policy(
        final String name,
        final Consumer<S> scenarioConsumer
    ) {
        this.name = name;
        this.scenarioConsumer = scenarioConsumer;
    }

    public String getName() {
        return name;
    }

    public Consumer<S> getScenarioConsumer() {
        return scenarioConsumer;
    }

    @Override
    public List<String> getHeaders() {
        return ImmutableList.of("name");
    }

    @Override
    public Iterable<? extends Collection<?>> getRows() {
        return ImmutableList.of(ImmutableList.of(name));
    }
}
