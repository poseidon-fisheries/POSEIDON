/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.agents.market;

import uk.ac.ox.poseidon.agents.catches.CategorisedCatch;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.time.LocalDateTime;
import java.util.List;

/** A place, at a {@link Port}, where a vessel sells its catch. */
public interface Market {

    /** @return this market's identifying code */
    String getCode();

    /** @return the port this market is located at */
    Port getPort();

    /**
     * @param vessel            the selling vessel
     * @param categorisedCatch  the catch offered for sale
     * @param dateTime          when the sale happens
     * @return the resulting sale, recording what sold and what didn't
     */
    Sale sell(
        Vessel vessel,
        CategorisedCatch categorisedCatch,
        LocalDateTime dateTime
    );

    /**
     * Prices {@code categorisedCatch} as {@link #sell} would, without selling it: nothing is
     * broadcast and no sale ID is used.
     *
     * @param categorisedCatch the catch to price
     * @return the items that would sell, one per priced (category, species) combination; what
     * has no price is left out
     */
    List<Sale.Item> quote(CategorisedCatch categorisedCatch);

}
