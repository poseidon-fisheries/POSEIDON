/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import java.util.Map.Entry;
import java.util.function.BiFunction;

/**
 * The idea of a "catch maker" is that you pass it a biology representing the available fish and a
 * biology representing the amount of fish that you aim to catch. It returns a pair containing a
 * {@link Catch} object with the fish it was able to catch and a new biology with the "uncaught"
 * fish: the unfulfilled part of the "desired" catch, given what was available.
 * <p>
 * The intended use for classes implementing this interface is to allow school set actions to fall
 * back on FAD biologies if there isn't enough fish available in the local ocean tile.
 *
 * @param <B> The type of biology to operate on
 */
@FunctionalInterface
public interface CatchMaker<B extends LocalBiology> extends BiFunction<B, B, Entry<Catch, B>> {

    @Override
    Entry<Catch, B> apply(B availableBiology, B desiredBiology);
}
