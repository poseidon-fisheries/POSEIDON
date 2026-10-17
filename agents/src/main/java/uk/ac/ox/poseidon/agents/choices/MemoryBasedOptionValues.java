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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.util.Map.entry;
import static lombok.AccessLevel.PACKAGE;
import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import it.unimi.dsi.fastutil.objects.ObjectDoubleBiConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * The {@link OptionValues} of a vessel, read from its {@link Memory}: the memory
 * used is the one the memory selector gives for the vessel when its option
 * values are asked for (e.g. a {@link KeyedMemorySelector} picking the memory
 * for the vessel's current gear), and each option is worth the valuation, for
 * that vessel, of that option given what is remembered about it. A value only
 * makes sense in a context (where the vessel is, the prices it faces, its
 * gear...), which the valuation reads from the vessel.
 * <p>
 * The option values given for a vessel serve one pick and are not to be kept:
 * their memory is the one selected when they were given, which a later change
 * (of gear, say) can make the wrong one. The values themselves are worked out
 * afresh on every call, with nothing cached, so a change of context (e.g. a
 * move, new prices) shows straight away, without any new observation. Finding
 * the best options therefore values every remembered option each time.
 *
 * @param <O> the type of option valued
 * @param <M> the type of what is remembered about an option
 */
@RequiredArgsConstructor(access = PACKAGE)
public class MemoryBasedOptionValues<
  O,
  M
> implements Function<Vessel, OptionValues<O>> {

  private final Function<? super Vessel, ? extends Memory<O, M>> memorySelector;
  private final Valuation<? super O, ? super M> valuation;

  /**
   * @param vessel the vessel whose option values are asked for
   * @return the option values of {@code vessel}, for one pick
   */
  @Override
  public OptionValues<O> apply(final Vessel vessel) {
    return new View(vessel, memorySelector.apply(vessel));
  }

  @RequiredArgsConstructor
  private final class View implements OptionValues<O> {

    private final Vessel vessel;
    private final Memory<O, M> memory;

    @Override
    public Optional<Double> getValue(final O option) {
      return memory
        .get(option)
        .map(recollection -> valuation.valueOf(vessel, option, recollection));
    }

    @Override
    public void forEachEntry(final ObjectDoubleBiConsumer<? super O> consumer) {
      memory.forEach((option, recollection) ->
        consumer.accept(option, valuation.valueOf(vessel, option, recollection))
      );
    }

    @Override
    public List<O> getBestOptions() {
      return getBestEntries()
        .stream()
        .map(Entry::getKey)
        .collect(toImmutableList());
    }

    @Override
    public Optional<O> getBestOption(final MersenneTwisterFast rng) {
      return getBestEntry(rng).map(Entry::getKey);
    }

    @Override
    public Optional<Double> getBestValue() {
      return getBestEntries().stream().findAny().map(Entry::getValue);
    }

    @Override
    public List<Entry<O, Double>> getBestEntries() {
      final List<Entry<O, Double>> bestEntries = new ArrayList<>();
      final double[] bestValue = { NEGATIVE_INFINITY };
      forEachEntry((option, value) -> {
        if (value > bestValue[0]) {
          bestValue[0] = value;
          bestEntries.clear();
          bestEntries.add(entry(option, value));
        } else if (value == bestValue[0]) {
          bestEntries.add(entry(option, value));
        }
      });
      return ImmutableList.copyOf(bestEntries);
    }

    @Override
    public Optional<Entry<O, Double>> getBestEntry(
      final MersenneTwisterFast rng
    ) {
      final List<Entry<O, Double>> bestEntries = getBestEntries();
      return bestEntries.isEmpty()
        ? Optional.empty()
        : Optional.of(oneOf(bestEntries, rng));
    }
  }
}
