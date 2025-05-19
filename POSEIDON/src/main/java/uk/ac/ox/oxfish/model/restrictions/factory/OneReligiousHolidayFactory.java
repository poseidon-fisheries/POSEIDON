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
import uk.ac.ox.oxfish.model.restrictions.RegionalRestrictions;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

public class OneReligiousHolidayFactory implements AlgorithmFactory<RegionalRestrictions> {

    private DoubleParameter startDayOfYear = new FixedDoubleParameter(1);
    private DoubleParameter endDayOfYear = new FixedDoubleParameter(180);

    private DoubleParameter upperLeftCornerX = new FixedDoubleParameter(0);
    private DoubleParameter upperLeftCornerY = new FixedDoubleParameter(33);
    private DoubleParameter lowerRightCornerX = new FixedDoubleParameter(49);
    private DoubleParameter lowerRightCornerY = new FixedDoubleParameter(49);


    public DoubleParameter getStartDayOfYear() {
        return startDayOfYear;
    }

    public void setStartDayOfYear(DoubleParameter startDayOfYear) {
        this.startDayOfYear = startDayOfYear;
    }

    public DoubleParameter getEndDayOfYear() {
        return endDayOfYear;
    }

    public void setEndDayOfYear(DoubleParameter endDayOfYear) {
        this.endDayOfYear = endDayOfYear;
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
    public RegionalRestrictions apply(FishState t) {
        RegionalRestrictions restrictions = new RegionalRestrictions();
        restrictions.addAnnualRestriction(
            t.getMap().getSeaTile(
                (int) upperLeftCornerX.applyAsDouble(t.random),
                (int) upperLeftCornerY.applyAsDouble(t.random)
            ),
            t.getMap().getSeaTile(
                (int) lowerRightCornerX.applyAsDouble(t.random),
                (int) lowerRightCornerY.applyAsDouble(t.random)
            ),
            (int) startDayOfYear.applyAsDouble(t.random),
            (int) endDayOfYear.applyAsDouble(t.random)
        );
        return restrictions;
    }

}
