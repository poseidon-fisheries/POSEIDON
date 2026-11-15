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

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import java.util.function.Function;
import uk.ac.ox.poseidon.core.events.AbstractListener;
import uk.ac.ox.poseidon.core.events.HasDateTime;

/**
 * Records an event's observation under its extracted key, in the entries
 * selected from the event, with its rule and the event's own time. An absent
 * key skips the event without extracting an observation or selecting memory.
 * Registration is the caller's responsibility and decides whose events it
 * hears; constructing a recorder does not register it.
 * @param <E> the event type
 * @param <K> the entry key type
 * @param <T> the observation type
 * @param <M> the remembered type
 */
public class EventRecorder<
  E extends HasDateTime,
  K,
  T,
  M
> extends AbstractListener<E> {

  private final Function<? super E, ? extends Optional<K>> key;
  private final Function<? super E, ? extends T> observation;
  private final Function<
    ? super E,
    ? extends MutableMemoryEntries<K, M>
  > memory;
  private final UpdateRule<? super T, M> updateRule;

  EventRecorder(
    final Class<E> eventClass,
    final Function<? super E, ? extends Optional<K>> key,
    final Function<? super E, ? extends T> observation,
    final Function<? super E, ? extends MutableMemoryEntries<K, M>> memory,
    final UpdateRule<? super T, M> updateRule
  ) {
    super(eventClass);
    this.key = requireNonNull(key);
    this.observation = requireNonNull(observation);
    this.memory = requireNonNull(memory);
    this.updateRule = requireNonNull(updateRule);
  }

  @Override
  public void receive(final E event) {
    key
      .apply(event)
      .ifPresent(key ->
        memory
          .apply(event)
          .record(
            key,
            observation.apply(event),
            event.getDateTime(),
            updateRule
          )
      );
  }
}
