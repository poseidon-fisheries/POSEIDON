/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.behaviours.choices;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import one.util.streamex.EntryStream;
import uk.ac.ox.poseidon.agents.registers.Register;
import uk.ac.ox.poseidon.agents.registers.TransformedRegister;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

import java.util.Map.Entry;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BestOptionsRegisterFactory<O>
    extends SimulationScopeFactory<Register<Entry<O, Double>>> {

    Factory<? extends Register<? extends OptionValues<O>>> optionValuesRegister;

    @Override
    protected Register<Entry<O, Double>> newInstance(final Simulation simulation) {
        return new TransformedRegister<>(
            optionValuesRegister.get(simulation),
            entryStream -> EntryStream
                .of(entryStream)
                .flatMapValues(optionValues -> optionValues.getBestEntries().stream())
        );
    }
}
