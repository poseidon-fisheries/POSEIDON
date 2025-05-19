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

package uk.ac.ox.oxfish.model.restrictions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.ArrayList;
import java.util.List;

/**
 * A list of geographic restrictions - they could be of any type
 * These are generic rectangles.
 *
 * @author Brian Powers
 */
public class RegionalRestrictions implements Restriction {

    private List<RestrictedRectangularRegion> restrictions = new ArrayList<>();

    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        boolean isOK = true;
        for (RestrictedRectangularRegion restriction : restrictions) {
            if (!restriction.canIFishHere(model, tile)) {
                isOK = false;
                break;
            }
        }
        return isOK;
    }

    public void addEternalRestriction(SeaTile nwCorner, SeaTile seCorner) {
        RestrictedRectangularRegion restriction = new RestrictedRectangularRegion(nwCorner, seCorner, 1);
        this.restrictions.add(restriction);
    }

    public void addOneTimeRestriction(SeaTile nwCorner, SeaTile seCorner, int onDay, int offDay) {
        RestrictedRectangularRegion restriction = new RestrictedRectangularRegion(nwCorner, seCorner, 2, onDay, offDay);
        this.restrictions.add(restriction);
    }

    public void addAnnualRestriction(SeaTile nwCorner, SeaTile seCorner, int onDay, int offDay) {
        RestrictedRectangularRegion restriction = new RestrictedRectangularRegion(nwCorner, seCorner, 3, onDay, offDay);
        this.restrictions.add(restriction);
    }

    public void addAnnualRestriction(
        SeaTile nwCorner,
        SeaTile seCorner,
        int onMonth,
        int onDay,
        int offMonth,
        int offDay
    ) {
        RestrictedRectangularRegion restriction = new RestrictedRectangularRegion(
            nwCorner,
            seCorner,
            3,
            onMonth,
            onDay,
            offMonth,
            offDay
        );
        this.restrictions.add(restriction);
    }

    public void clearRestrictions() {
        this.restrictions.clear();
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        // TODO Auto-generated method stub

    }

    @Override
    public void turnOff(Fisher fisher) {
        // TODO Auto-generated method stub

    }

}
