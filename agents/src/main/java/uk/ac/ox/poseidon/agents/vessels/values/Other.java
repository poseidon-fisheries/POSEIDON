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

package uk.ac.ox.poseidon.agents.vessels.values;

import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.Vessel;

/**
 * A value of a vessel read off the other vessel: in a test that relates the
 * vessel a call is for (the context) to another vessel (the thing mapped), it
 * reads the value off that other vessel and ignores the context. A value of a
 * vessel used there without it reads the vessel the call is for, so the other
 * vessel is always named.
 *
 * @param <R> the type of the value
 */
@RequiredArgsConstructor
public class Other<R> implements BiFunction<Object, Vessel, R> {

  private final @NonNull Function<? super Vessel, ? extends R> value;

  @Override
  public R apply(final Object context, final Vessel otherVessel) {
    return value.apply(otherVessel);
  }
}
