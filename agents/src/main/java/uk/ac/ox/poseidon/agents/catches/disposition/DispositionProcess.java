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

package uk.ac.ox.poseidon.agents.catches.disposition;

import uk.ac.ox.poseidon.biology.buckets.Bucket;

/**
 * One step of how a catch gets sorted into retained/discarded-alive/discarded-dead. Several steps
 * compose into a full {@link CompositeDispositionProcess}, each refining the previous one's
 * {@link Disposition}.
 */
public interface DispositionProcess {

    /**
     * @param currentDisposition   the disposition so far, refined further by this step
     * @param availableCapacityInKg how much hold capacity remains
     * @return the refined disposition
     */
    Disposition partition(
        Disposition currentDisposition,
        double availableCapacityInKg
    );

    /**
     * @param grossCatch            the whole gross catch, treated as fully retained to start with
     * @param availableCapacityInKg how much hold capacity remains
     * @return the disposition after this step, starting from {@code grossCatch} entirely retained
     */
    default Disposition partition(
        final Bucket grossCatch,
        final double availableCapacityInKg
    ) {
        return partition(
            new Disposition(grossCatch, Bucket.empty(), Bucket.empty()),
            availableCapacityInKg
        );
    }

}
