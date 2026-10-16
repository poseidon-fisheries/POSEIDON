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

package uk.ac.ox.poseidon.agents.choices;

import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Factory;

/**
 * A {@link VesselScopeFactory} counterpart of {@link BestKnownOption}, built
 * via {@link Factories#bestKnownOption(Factory, Factory)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class BestKnownOptionFactory<O> extends VesselScopeFactory<
  BestKnownOption<O>
> {

  private final Factory<
    ? super VesselScope,
    ? extends OptionValues<O>
  > optionValues;
  private final Factory<? super VesselScope, ? extends Supplier<O>> fallback;

  @Override
  protected BestKnownOption<O> newInstance(final VesselScope scope) {
    return new BestKnownOption<>(
      optionValues.get(scope),
      fallback.get(scope),
      scope.getSimulation().random
    );
  }
}
