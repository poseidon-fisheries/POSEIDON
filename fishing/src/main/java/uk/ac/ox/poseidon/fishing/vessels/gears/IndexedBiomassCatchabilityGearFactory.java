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

package uk.ac.ox.poseidon.fishing.vessels.gears;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.functions.VoidContextConversions.toFunction;
import static uk.ac.ox.poseidon.core.functions.VoidContextConversions.toSupplier;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkUnitRange;

import java.time.Duration;
import java.util.Collection;
import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.biology.species.SpeciesIndex;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * A {@link RelativeScopeFactory} counterpart of
 * {@link IndexedBiomassCatchabilityGear}, built via
 * {@link Factories#indexedBiomassCatchabilityGear}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class IndexedBiomassCatchabilityGearFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  IndexedBiomassCatchabilityGear
> {

  private final ValueFactory<? super S, ? super Void, Duration> duration;
  private final Factory<
    ? super S,
    ? extends Collection<? extends Species>
  > species;

  private final MappingFactory<
    ? super S,
    ? super Void,
    ? super Species,
    Double
  > proportionFunction;
  private final Factory<
    ? super S,
    ? extends Quantity<Mass>
  > minimumCatchThreshold;

  @Override
  protected IndexedBiomassCatchabilityGear newInstance(final S scope) {
    final var function = toFunction(proportionFunction.get(scope));
    return new IndexedBiomassCatchabilityGear(
      SpeciesIndex.of(species.get(scope)).mapToDoubleArray(s ->
        checkUnitRange(function.apply(s), "proportion")
      ),
      minimumCatchThreshold.get(scope).to(KILOGRAM).getValue().doubleValue(),
      toSupplier(duration.get(scope))
    );
  }
}
