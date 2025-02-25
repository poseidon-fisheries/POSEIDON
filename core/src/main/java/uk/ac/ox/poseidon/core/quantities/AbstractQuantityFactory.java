/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core.quantities;

import lombok.*;
import si.uom.NonSI;
import tech.units.indriya.format.SimpleQuantityFormat;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.quantity.Quantities;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import javax.measure.Quantity;
import javax.measure.Unit;
import java.util.Map.Entry;

import static java.util.Map.entry;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
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
    protected Quantity<Q> newInstance(final @NonNull Simulation simulation) {
        final Unit<Q> unit = SimpleUnitFormat.getInstance().parse(unitString).asType(type);
        return Quantities.getQuantity(value, unit);
    }
}
