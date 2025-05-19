/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;
import java.util.List;

public class TaggedRegulationFactory
    extends DecoratedObjectFactory<AlgorithmFactory<? extends Regulation>>
    implements AlgorithmFactory<TaggedRegulation> {

    private List<String> tags;

    public TaggedRegulationFactory() {
    }

    public TaggedRegulationFactory(
        final AlgorithmFactory<? extends Regulation> delegate,
        final String... tags
    ) {
        this(delegate, ImmutableList.copyOf(tags));
    }

    @SuppressWarnings("WeakerAccess")
    public TaggedRegulationFactory(
        final AlgorithmFactory<? extends Regulation> delegate,
        final List<String> tags
    ) {
        super(delegate);
        this.tags = tags;
    }

    public Collection<String> getTags() {
        return tags;
    }

    public void setTags(final List<String> tags) {
        this.tags = tags;
    }

    @Override
    public TaggedRegulation apply(final FishState fishState) {
        return new TaggedRegulation(getDelegate().apply(fishState), tags);
    }
}
