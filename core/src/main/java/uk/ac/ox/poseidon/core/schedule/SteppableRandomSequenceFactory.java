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

package uk.ac.ox.poseidon.core.schedule;

import lombok.Data;
import lombok.EqualsAndHashCode;
import sim.engine.RandomSequence;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.Factory;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class SteppableRandomSequenceFactory extends AbtractSteppableSequenceFactory {
    public SteppableRandomSequenceFactory(final List<? extends Factory<? extends Steppable>> steppables) {
        super(steppables);
    }

    public SteppableRandomSequenceFactory() {
    }

    @SafeVarargs
    public SteppableRandomSequenceFactory(final Factory<? extends Steppable>... steppables) {
        super(steppables);
    }

    @Override
    protected Steppable newSequence(final List<? extends Steppable> steppables) {
        return new RandomSequence(steppables);
    }
}
