/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.network;

import uk.ac.ox.oxfish.fisher.Fisher;

import java.io.Serializable;

/**
 * A true/false check on whether a connection is allowed
 * Created by carrknight on 2/12/16.
 */
public interface NetworkPredicate extends Serializable {


    /**
     * Used by the network builder to see if this friendship is allowed.
     *
     * @param from origin
     * @param to   destination
     * @return true if the connection can be built
     */
    boolean test(Fisher from, Fisher to);
}
