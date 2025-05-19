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

package uk.ac.ox.oxfish.model.restrictions.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.restrictions.ReputationalRestrictions;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class RandomTerritoryFactory implements AlgorithmFactory<ReputationalRestrictions> {

    private DoubleParameter numberOfTerritorySeaTiles = new FixedDoubleParameter(5);
    private DoubleParameter upperLeftCornerX = new FixedDoubleParameter(0);
    private DoubleParameter upperLeftCornerY = new FixedDoubleParameter(0);
    private DoubleParameter lowerRightCornerX = new FixedDoubleParameter(49);
    private DoubleParameter lowerRightCornerY = new FixedDoubleParameter(16);

    public DoubleParameter getNumberOfTerritorySeaTiles() {
        return numberOfTerritorySeaTiles;
    }

    public void setNumberOfTerritorySeaTiles(DoubleParameter numberOfTerritorySeaTiles) {
        this.numberOfTerritorySeaTiles = numberOfTerritorySeaTiles;
    }

    public DoubleParameter getUpperLeftCornerX() {
        return upperLeftCornerX;
    }

    public void setUpperLeftCornerX(DoubleParameter value) {
        this.upperLeftCornerX = value;
    }

    public DoubleParameter getUpperLeftCornerY() {
        return upperLeftCornerY;
    }

    public void setUpperLeftCornerY(DoubleParameter value) {
        this.upperLeftCornerY = value;
    }

    public DoubleParameter getLowerRightCornerX() {
        return lowerRightCornerX;
    }

    public void setLowerRightCornerX(DoubleParameter value) {
        this.lowerRightCornerX = value;
    }

    public DoubleParameter getLowerRightCornerY() {
        return lowerRightCornerY;
    }

    public void setLowerRightCornerY(DoubleParameter value) {
        this.lowerRightCornerY = value;
    }


    @Override
    public ReputationalRestrictions apply(FishState t) {
        ReputationalRestrictions restrictions = new ReputationalRestrictions();
        int nTerr = (int) numberOfTerritorySeaTiles.applyAsDouble(t.random);
        //System.out.println("Giving "+nTerr+" territories");
        restrictions.addTerritories(t.getMap(), t.getRandom(),
            nTerr,
            (int) upperLeftCornerX.applyAsDouble(t.random),
            (int) upperLeftCornerY.applyAsDouble(t.random),
            (int) lowerRightCornerX.applyAsDouble(t.random),
            (int) lowerRightCornerY.applyAsDouble(t.random)
        );
        return restrictions;
    }
}
