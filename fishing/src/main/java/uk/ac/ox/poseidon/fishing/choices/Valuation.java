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

package uk.ac.ox.poseidon.fishing.choices;

import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Values options for a vessel, given what is remembered about each: what the
 * remembered catch would sell for at the vessel's home port, what it would cost
 * the vessel to travel there, or a combination of such valuations. A valuation
 * that does not depend on the option, or on what is remembered, ignores it, so
 * that it can be combined with others of the same shape.
 * <p>
 * Valuing happens in two stages. Put in the context of a vessel, a valuation
 * takes stock of the vessel's circumstances at the moment of choosing (its
 * hourly cost, its home port's market...); the {@link ContextualisedValuation}
 * it gives then values each option the vessel compares under those
 * circumstances, which are the same for every option of one choice and are so
 * read only once.
 * <p>
 * A contextualised valuation is used synchronously, for one view of the
 * vessel's option values, and is not kept beyond it: the circumstances it read
 * may since have changed. The model state it reads must not change while it is
 * used. A valuation does not change the vessel, the market or any other model
 * state, though it may keep a store of its own, and the value it gives an
 * option does not depend on the order in which options are valued.
 *
 * @param <O> the type of option valued
 * @param <M> the type of what is remembered about an option
 */
@FunctionalInterface
public interface Valuation<O, M> {
  /**
   * @param vessel the vessel options are valued for
   * @return the valuation of options for {@code vessel} in its current
   *         circumstances, to use for one view of its option values
   */
  ContextualisedValuation<O, M> inContextOf(Vessel vessel);
}
