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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

import java.util.concurrent.atomic.AtomicLong;

public class AbundanceFadAttractionEvent {

    private static final AtomicLong nextId = new AtomicLong();
    private final long id = nextId.getAndIncrement();
    private final AbundanceAggregatingFad fad;
    private final AbundanceLocalBiology tileAbundanceBefore;
    private final AbundanceLocalBiology fadAbundanceDelta;

    public AbundanceFadAttractionEvent(
        final AbundanceAggregatingFad fad,
        final AbundanceLocalBiology tileAbundanceBefore,
        final AbundanceLocalBiology fadAbundanceDelta
    ) {
        this.fad = fad;
        this.tileAbundanceBefore = tileAbundanceBefore;
        this.fadAbundanceDelta = fadAbundanceDelta;
    }

    public long getId() {
        return id;
    }

    public AbundanceAggregatingFad getFad() {
        return fad;
    }

    public AbundanceLocalBiology getTileAbundanceBefore() {
        return tileAbundanceBefore;
    }

    public AbundanceLocalBiology getFadAbundanceDelta() {
        return fadAbundanceDelta;
    }
}
