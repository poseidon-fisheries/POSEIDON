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

import static lombok.AccessLevel.PACKAGE;

import it.unimi.dsi.fastutil.objects.ObjectDoubleBiConsumer;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * Reads remembered outcomes as option values, valuing each outcome for the
 * receiving vessel. Each visit selects the vessel's entries anew and reads
 * valuation circumstances once. Options without evidence are not visited.
 * @param <O> the option type
 * @param <M> the remembered outcome type
 */
@RequiredArgsConstructor(access = PACKAGE)
public class MemoryBasedOptionValues<O, M> implements OptionValues<O> {

  private final @NonNull Function<
    ? super Vessel,
    ? extends MemoryEntries<O, M>
  > memorySelector;
  private final @NonNull Valuation<? super M> valuation;

  @Override
  public void forEachEntry(
    final Vessel vessel,
    final ObjectDoubleBiConsumer<? super O> consumer
  ) {
    final MemoryEntries<O, M> memory = memorySelector.apply(vessel);
    final ToDoubleFunction<? super M> contextualisedValuation = valuation.by(
      vessel
    );
    memory.forEachEntry((option, recollection) ->
      consumer.accept(
        option,
        contextualisedValuation.applyAsDouble(recollection)
      )
    );
  }
}
