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

package uk.ac.ox.poseidon.agents.vessels.gears;

import si.uom.quantity.VolumetricFlowRate;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.time.Duration;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public class Factories {

    private Factories() {}

    public static <S extends SimulationScope> FixedBiomassProportionGearFactory<S>
    fixedBiomassProportionGear(
        final String code,
        final double proportion,
        final Factory<? super S, ? extends Quantity<Mass>> minimumCatchThreshold,
        final Factory<? super S, ? extends Supplier<Duration>> durationSupplier,
        final Factory<? super S, ? extends Quantity<VolumetricFlowRate>>
            fuelConsumptionRate
    ) {
        return new FixedBiomassProportionGearFactory<>(
            code, proportion, minimumCatchThreshold, durationSupplier, fuelConsumptionRate
        );
    }

    public static InactiveGearFactory inactiveGear(final String code) {
        return new InactiveGearFactory(code);
    }

    public static <S extends Scope> IndexedBiomassCatchabilityGearFactory<S>
    indexedBiomassCatchabilityGear(
        final String code,
        final Factory<? super S, ? extends Supplier<Duration>> durationSupplier,
        final Factory<? super S, ? extends Collection<? extends Species>> species,
        final Factory<? super S, ? extends Function<? super Species, Double>> proportionFunction,
        final Factory<? super S, ? extends Quantity<Mass>> minimumCatchThreshold
    ) {
        return new IndexedBiomassCatchabilityGearFactory<S>(
            code, durationSupplier, species, proportionFunction, minimumCatchThreshold
        );
    }
}
