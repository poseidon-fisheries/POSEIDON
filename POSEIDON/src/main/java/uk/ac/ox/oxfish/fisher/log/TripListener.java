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

package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.fisher.Fisher;

import java.io.Serializable;

/**
 * Any object that needs to be notified a trip is over
 * Created by carrknight on 6/17/15.
 */
public interface TripListener extends Serializable {
    long serialVersionUID = 3423123730692395603L;

    default void reactToNewTrip(final TripRecord record, final Fisher fisher) {
    }

    void reactToFinishedTrip(TripRecord record, Fisher fisher);

}
