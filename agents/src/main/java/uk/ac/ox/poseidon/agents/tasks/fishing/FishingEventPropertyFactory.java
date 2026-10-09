/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.tasks.fishing;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.functions.ObjectProperty;
import uk.ac.ox.poseidon.core.functions.ObjectPropertyFactory;

/**
 * An {@link ObjectPropertyFactory} for an {@link ObjectProperty} of a {@link FishingEvent}, built
 * via {@link Factories#fishingEventProperty}.
 *
 * @param <R> the type of the property; not checked
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FishingEventPropertyFactory<R> extends ObjectPropertyFactory<FishingEvent, R> {

    /** @param propertyPath the dotted path of properties to follow from the fishing event */
    public FishingEventPropertyFactory(final String propertyPath) {
        super(propertyPath);
    }

    @Override
    protected Class<FishingEvent> rootClass() {
        return FishingEvent.class;
    }
}
