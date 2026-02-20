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

package uk.ac.ox.poseidon.agents.vessels.engines;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import javax.measure.Quantity;
import javax.measure.quantity.Volume;

import static tech.units.indriya.unit.Units.LITRE;
import static uk.ac.ox.poseidon.core.quantities.Factories.volumeOf;

public class Factories {

    public static <S extends Scope> SimpleFuelTankFactory<S> fullTank(
        final Factory<? super S, ? extends Quantity<Volume>> capacity
    ) {
        return new SimpleFuelTankFactory<>(capacity, capacity);
    }

    public static <S extends Scope> SimpleFuelTankFactory<S> emptyTank(
        final Factory<? super S, ? extends Quantity<Volume>> capacity
    ) {
        return new SimpleFuelTankFactory<>(capacity, volumeOf(0, LITRE));
    }

}
