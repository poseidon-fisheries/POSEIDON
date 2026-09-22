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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import si.uom.NonSI;
import tech.units.indriya.format.SimpleQuantityFormat;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.quantity.Quantities;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.Map.Entry;

import static java.util.Map.entry;

/**
 * A {@link GlobalScopeFactory} that parses a unit string (e.g. {@code "kg"}, {@code "m/s"}) and a
 * numeric value into a {@link Quantity} of the declared type {@code Q}. No separate plain
 * component class here: the produced value is a bare JSR-385 {@link Quantity}, with no domain
 * wrapper to carry documentation. Package-private template base for the leaf {@code *Factory}
 * classes ({@link MassFactory}, {@link VolumeFactory}, {@link SpeedFactory},
 * {@link VolumetricFlowRateFactory}), each of which just fixes {@code Q} and exposes the
 * no-args/all-args constructors SnakeYAML and {@link Factories} need.
 *
 * @param <Q> the JSR-385 quantity type this factory produces
 */
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class AbstractQuantityFactory<Q extends Quantity<Q>>
    extends GlobalScopeFactory<Quantity<Q>> {

    static {
        // We need to trigger static initialization of the NonSI class
        // in order for non-SI unit string formats to be registered
        // noinspection ResultOfMethodCallIgnored
        NonSI.getInstance();
    }

    private final Class<Q> type;
    private double value;
    private String unitString;

    /**
     * @param type     the quantity type to parse the string as
     * @param quantity a quantity string in the format produced by {@code Quantity.toString()}
     *                 (e.g. {@code "3.5 kg"})
     * @return the parsed value paired with its unit's string representation
     */
    static <Q extends Quantity<Q>> Entry<String, Double> parse(
        final Class<Q> type,
        final String quantity
    ) {
        final Quantity<Q> q = SimpleQuantityFormat.getInstance().parse(quantity).asType(type);
        return entry(
            q.getUnit().toString(),
            q.getValue().doubleValue()
        );
    }

    @Override
    protected Quantity<Q> newInstance(final Scope scope) {
        final Unit<Q> unit = SimpleUnitFormat.getInstance().parse(unitString).asType(type);
        return Quantities.getQuantity(value, unit);
    }
}
