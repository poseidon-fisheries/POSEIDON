/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.agents.vessels.hold;

import lombok.Data;
import uk.ac.ox.poseidon.biology.Bucket;
import uk.ac.ox.poseidon.biology.Content;

public interface OvercapacityDiscardingStrategy<C extends Content<C>> {
    Result<C> discard(
        Bucket<C> contentToAdd,
        Bucket<C> currentHoldContent,
        double capacityInKg,
        double toleranceInKg
    );

    @Data
    class Result<C extends Content<C>> {
        public final Bucket<C> discarded;
        public final Bucket<C> updatedHoldContent;
    }
}
