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

package uk.ac.ox.poseidon.fishing.choices;

import ec.util.MersenneTwisterFast;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory;

/**
 * A {@link VesselScopeFactory} counterpart of {@link GreedyPicker}, built via
 * {@link Factories#greedyPicker(Factory, MappingFactory)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class GreedyPickerFactory<O>
  extends VesselScopeFactory<Function<Vessel, O>>
  implements ValueFactory<VesselScope, Vessel, O>
{

  private final Factory<
    ? super VesselScope,
    ? extends Function<? super Vessel, ? extends OptionValues<O>>
  > optionValues;
  private final MappingFactory<
    ? super VesselScope,
    ? super Vessel,
    ? super O,
    Boolean
  > optionPredicate;
  private final Factory<
    ? super VesselScope,
    ? extends MersenneTwisterFast
  > randomNumberGenerator;

  @Override
  protected Function<Vessel, O> newInstance(final VesselScope scope) {
    return new GreedyPicker<>(
      optionValues.get(scope),
      optionPredicate.get(scope),
      randomNumberGenerator.get(scope)
    );
  }
}
