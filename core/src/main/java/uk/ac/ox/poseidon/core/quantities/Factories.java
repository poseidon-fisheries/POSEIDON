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

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import si.uom.quantity.VolumetricFlowRate;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Mass;
import javax.measure.quantity.Speed;
import javax.measure.quantity.Volume;

import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.quantities.AbstractQuantityFactory.parse;

public class Factories {

    private Factories() {
    }

    public static <S extends Scope> KilogramsFactory<S> kilograms(
        final Factory<? super S, ? extends Quantity<Mass>> mass
    ) {
        return new KilogramsFactory<>(mass);
    }

    public static KilogramsFactory<Scope> kilograms(final double value) {
        return new KilogramsFactory<>(massOf(value, KILOGRAM));
    }

    public static MassFactory massOf(
        final double value,
        final Unit<Mass> unit
    ) {
        return new MassFactory(value, unit.toString());
    }

    public static MassFactory massOf(final Quantity<Mass> quantity) {
        return massOf(quantity.getValue().doubleValue(), quantity.getUnit());
    }

    public static MassFactory massOf(final String quantity) {
        final var entry = parse(Mass.class, quantity);
        return new MassFactory(entry.getValue(), entry.getKey());
    }

    public static VolumeFactory volumeOf(
        final double value,
        final Unit<Volume> unit
    ) {
        return new VolumeFactory(value, unit.toString());
    }

    public static VolumeFactory volumeOf(final Quantity<Volume> quantity) {
        return volumeOf(quantity.getValue().doubleValue(), quantity.getUnit());
    }

    public static VolumeFactory volumeOf(final String quantity) {
        final var entry = parse(Volume.class, quantity);
        return new VolumeFactory(entry.getValue(), entry.getKey());
    }

    public static SpeedFactory speedOf(final Quantity<Speed> quantity) {
        return new SpeedFactory(quantity.getValue().doubleValue(), quantity.getUnit().toString());
    }

    public static SpeedFactory speedOf(
        final double value,
        final Unit<Speed> unit
    ) {
        return speedOf(getQuantity(value, unit));
    }

    public static SpeedFactory speedOf(final String quantity) {
        final var entry = parse(Speed.class, quantity);
        return new SpeedFactory(entry.getValue(), entry.getKey());
    }

    public static VolumetricFlowRateFactory volumetricFlowRateOf(
        final double value,
        final Unit<VolumetricFlowRate> unit
    ) {
        return new VolumetricFlowRateFactory(value, unit);
    }

    public static VolumetricFlowRateFactory volumetricFlowRateOf(
        final Quantity<VolumetricFlowRate> quantity
    ) {
        return volumetricFlowRateOf(quantity.getValue().doubleValue(), quantity.getUnit());
    }

    public static VolumetricFlowRateFactory volumetricFlowRateOf(final String quantity) {
        final var entry = parse(VolumetricFlowRate.class, quantity);
        return new VolumetricFlowRateFactory(entry.getValue(), entry.getKey());
    }

}
