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

package uk.ac.ox.poseidon.core.time;

import lombok.*;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
abstract class RelativeDateTimeFactory implements Factory<LocalDateTime> {

    @NonNull private Factory<? extends LocalDateTime> referenceDateTimeFactory;
    @NonNull private Factory<? extends TemporalAmount> temporalAmountFactory;

    @Override
    public LocalDateTime get(final Simulation simulation) {
        return operation(
            referenceDateTimeFactory.get(simulation),
            temporalAmountFactory.get(simulation)
        );
    }

    protected abstract LocalDateTime operation(
        LocalDateTime referenceDateTime,
        TemporalAmount temporalAmount
    );
}