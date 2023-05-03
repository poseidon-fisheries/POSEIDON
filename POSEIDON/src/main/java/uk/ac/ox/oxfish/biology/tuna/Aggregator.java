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

import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AggregatingFad;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collection;
import java.util.function.BiFunction;

/**
 * Objects of this class can take a collection of local biologies and "aggregate" then (i.e., sum
 * them) into a single local biology object.
 *
 * <p>In most cases, the collection of local biologies will be all the {@link SeaTile} biologies
 * plus all the {@link AggregatingFad} biologies.</p>
 *
 * @param <B> The type of local biology to aggregate.
 */
public abstract class Aggregator<B extends LocalBiology>
    implements BiFunction<GlobalBiology, Collection<B>, B> {

}
