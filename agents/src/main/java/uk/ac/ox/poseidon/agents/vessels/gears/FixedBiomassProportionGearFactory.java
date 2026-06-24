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

package uk.ac.ox.poseidon.agents.vessels.gears;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import si.uom.quantity.VolumetricFlowRate;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.time.Duration;
import java.util.function.Supplier;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.quantities.VolumetricFlowRateFactory.LITRE_PER_HOUR;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FixedBiomassProportionGearFactory<S extends SimulationScope>
    extends RelativeScopeFactory<S, FixedBiomassProportionGear> {

    private String code;
    private double proportion;
    private Factory<? super S, ? extends Quantity<Mass>> minimumCatchThreshold;
    private Factory<? super S, ? extends Supplier<Duration>> durationSupplier;
    private Factory<? super S, ? extends Quantity<VolumetricFlowRate>>
        fuelConsumptionRate;

    @Override
    protected FixedBiomassProportionGear newInstance(final S scope) {
        final double fuelPerHour = fuelConsumptionRate
            .get(scope)
            .to(LITRE_PER_HOUR)
            .getValue()
            .doubleValue();
        return new FixedBiomassProportionGear(
            code,
            proportion,
            minimumCatchThreshold.get(scope).to(KILOGRAM).getValue().doubleValue(),
            durationSupplier.get(scope),
            fuelPerHour
        );
    }

}
