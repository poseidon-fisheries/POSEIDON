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

package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.Collection;

public class SharedTripRecord {

    private TripRecord trip;
    private boolean allFriends = true;
    private Collection<Fisher> sharedFriends = null;

    public SharedTripRecord(TripRecord trip, boolean allFriends, Collection<Fisher> sharedFriends) {
        this.trip = trip;
        this.allFriends = allFriends;
        if (sharedFriends != null) {
            shareWithMoreFriends(sharedFriends);
        }
    }

    public void shareWithMoreFriends(Collection<Fisher> newSharedFriends) {
        for (Fisher newSharedFriend : newSharedFriends) {
            boolean addHim = true;
            for (Fisher oldSharedFriend : sharedFriends) {
                if (oldSharedFriend.equals(newSharedFriend)) {
                    addHim = false;
                    break;
                }
            }
            if (addHim) sharedFriends.add(newSharedFriend);
        }
    }

    public TripRecord getTrip() {
        return trip;
    }

    public void shareWithAllFriends() {
        allFriends = true;
    }

    public boolean sharedWithAll() {
        return this.allFriends;
    }

    public Collection<Fisher> getSharedFriends() {
        return sharedFriends;
    }

}
