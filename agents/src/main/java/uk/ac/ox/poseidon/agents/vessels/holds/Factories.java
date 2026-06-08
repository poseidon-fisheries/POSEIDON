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

package uk.ac.ox.poseidon.agents.vessels.holds;

import uk.ac.ox.poseidon.agents.catches.CatchCategoriser;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;

public class Factories {

    private Factories() {}

    public static InfiniteBiomassHoldFactory infiniteBiomassHold(
        final Factory<? super VesselScope, ? extends CatchCategoriser> catchCategoriser
    ) {
        return new InfiniteBiomassHoldFactory(catchCategoriser);
    }

    public static StandardBiomassHoldFactory standardBiomassHold(
        final Factory<? super VesselScope, ? extends Quantity<Mass>> capacity,
        final Factory<? super VesselScope, ? extends Quantity<Mass>> tolerance,
        final Factory<? super VesselScope, ? extends CatchCategoriser> catchCategoriser
    ) {
        return new StandardBiomassHoldFactory(capacity, tolerance, catchCategoriser);
    }
}
