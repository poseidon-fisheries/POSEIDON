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

package uk.ac.ox.poseidon.core.quantities;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.measure.Unit;
import javax.measure.quantity.Speed;

/**
 * A {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for {@link Speed} quantities, built via
 * {@link Factories Factories.speedOf(...)}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SpeedFactory extends AbstractQuantityFactory<Speed> {

    /** No-args constructor, used only by SnakeYAML during deserialization. */
    public SpeedFactory() {
        super(Speed.class);
    }

    /**
     * @param value      the numeric speed value
     * @param unitString the unit, in the format produced by {@code Unit.toString()}
     */
    public SpeedFactory(
        final double value,
        final String unitString
    ) {
        super(Speed.class, value, unitString);
    }

    /**
     * @param value the numeric speed value
     * @param unit  the unit the value is expressed in
     */
    public SpeedFactory(
        final double value,
        final Unit<Speed> unit
    ) {
        super(Speed.class, value, unit.toString());
    }

}
