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

package uk.ac.ox.poseidon.biology;

import uk.ac.ox.poseidon.biology.biomass.Biomass;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Mass;

/**
 * Represents quantitative content; implementations are expected to be immutable.
 */
public interface Content {

    /** @return this content plus {@code content}, as a {@link Biomass} */
    default Content add(final Content content) {
        return this.asBiomass().add(content.asBiomass());
    }

    /** @return this content minus {@code content}, as a {@link Biomass} */
    default Content subtract(final Content content) {
        return this.asBiomass().subtract(content.asBiomass());
    }

    /** @return this content's mass, in {@code biomassUnit} */
    default double as(final Unit<Mass> biomassUnit) {
        return this.asBiomass().as(biomassUnit);
    }

    /** @return this content scaled by {@code value} */
    Biomass multiply(double value);

    /** @return this content divided by {@code value} */
    Biomass divide(double value);

    /** @return whether this content has zero mass */
    boolean isEmpty();

    /** @return this content as a plain {@link Biomass} */
    Biomass asBiomass();

    /** @return this content's mass, as a {@link Quantity} */
    default Quantity<Mass> asQuantity() {
        return this.asBiomass().asQuantity();
    }

    /** @return this content's mass, in kilograms */
    default double asKg() {
        return this.asBiomass().asKg();
    }

}
