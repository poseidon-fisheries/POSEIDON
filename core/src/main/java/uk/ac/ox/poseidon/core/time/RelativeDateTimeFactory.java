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

package uk.ac.ox.poseidon.core.time;

import lombok.*;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;

/**
 * A {@link RelativeScopeFactory} template for a {@link LocalDateTime} computed by adding or
 * subtracting a resolved {@link TemporalAmount} from a resolved reference date-time; subclasses
 * ({@link DateTimeAfterFactory}, {@link DateTimeBeforeFactory}) supply the direction via
 * {@link #operation}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
abstract class RelativeDateTimeFactory<S extends Scope>
    extends RelativeScopeFactory<S, LocalDateTime> {

    @NonNull private Factory<? super S, ? extends LocalDateTime> referenceDateTime;
    @NonNull private Factory<? super S, ? extends TemporalAmount> temporalAmount;

    @Override
    protected LocalDateTime newInstance(final S scope) {
        return operation(
            referenceDateTime.get(scope),
            temporalAmount.get(scope)
        );
    }

    /**
     * @param referenceDateTime the resolved reference date-time
     * @param temporalAmount    the resolved amount
     * @return the reference date-time with the amount added or subtracted, per subclass direction
     */
    protected abstract LocalDateTime operation(
        LocalDateTime referenceDateTime,
        TemporalAmount temporalAmount
    );
}
