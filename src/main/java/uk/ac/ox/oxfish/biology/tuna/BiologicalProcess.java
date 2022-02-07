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

package uk.ac.ox.oxfish.biology.tuna;

import java.util.Collection;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A  class that takes some kind of local biology, does something with it, and optionally returns
 * the same kind of local biology. It is expected that the returned biology will be a copy.
 *
 * @param <B> The type of local biology to be acted upon.
 */
@FunctionalInterface
interface BiologicalProcess<B extends LocalBiology> {

    Collection<B> process(FishState fishState, Collection<B> biologies);

}
