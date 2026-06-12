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

package uk.ac.ox.poseidon.core.quantities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import si.uom.quantity.VolumetricFlowRate;

import javax.measure.Unit;

import static tech.units.indriya.unit.Units.HOUR;
import static tech.units.indriya.unit.Units.LITRE;

@Data
@EqualsAndHashCode(callSuper = true)
public class VolumetricFlowRateFactory extends AbstractQuantityFactory<VolumetricFlowRate> {

    public static final Unit<VolumetricFlowRate> LITRE_PER_HOUR =
        LITRE.divide(HOUR).asType(VolumetricFlowRate.class);

    public VolumetricFlowRateFactory() {
        super(VolumetricFlowRate.class);
    }

    public VolumetricFlowRateFactory(
        final double value,
        final String unitString
    ) {
        super(VolumetricFlowRate.class, value, unitString);
    }

    public VolumetricFlowRateFactory(
        final double value,
        final Unit<VolumetricFlowRate> unit
    ) {
        super(VolumetricFlowRate.class, value, unit.toString());
    }

}
