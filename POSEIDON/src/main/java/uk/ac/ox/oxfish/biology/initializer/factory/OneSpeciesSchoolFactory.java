/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.initializer.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.initializer.OneSpeciesInfiniteSchoolsInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.Map.Entry;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

/**
 * Creates the "chaser" biology with a school of fishers running around
 * Created by carrknight on 11/17/16.
 */
public class OneSpeciesSchoolFactory implements AlgorithmFactory<OneSpeciesInfiniteSchoolsInitializer> {


    private DoubleParameter numberOfSchools = new FixedDoubleParameter(1d);

    private String waypoints = "0,0 : 40,0 :  40,40: 0,40";

    private DoubleParameter startingX = new UniformDoubleParameter(0, 30);

    private DoubleParameter startingY = new UniformDoubleParameter(0, 30);

    private DoubleParameter diameter = new FixedDoubleParameter(4);

    private DoubleParameter speedInDays = new FixedDoubleParameter(10);

    private DoubleParameter biomassEach = new FixedDoubleParameter(10000);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public OneSpeciesInfiniteSchoolsInitializer apply(final FishState state) {
        final String[] splitString = waypoints.split(":");
        final Entry<Integer, Integer>[] trueWayPoints = new Entry[splitString.length];
        Preconditions.checkArgument(trueWayPoints.length >= 2);
        for (int i = 0; i < trueWayPoints.length; i++) {
            final String[] waypoint = splitString[i].split(",");
            assert waypoint.length == 2;
            trueWayPoints[i] = entry(
                Integer.parseInt(waypoint[0].trim()),
                Integer.parseInt(waypoint[1].trim())
            );
        }


        return new OneSpeciesInfiniteSchoolsInitializer(
            (int) numberOfSchools.applyAsDouble(state.getRandom()),
            trueWayPoints,
            startingX,
            startingY,
            diameter,
            speedInDays,
            biomassEach
        );

    }

    public DoubleParameter getNumberOfSchools() {
        return numberOfSchools;
    }

    public void setNumberOfSchools(final DoubleParameter numberOfSchools) {
        this.numberOfSchools = numberOfSchools;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(final String waypoints) {
        this.waypoints = waypoints;
    }

    public DoubleParameter getStartingX() {
        return startingX;
    }

    public void setStartingX(final DoubleParameter startingX) {
        this.startingX = startingX;
    }

    public DoubleParameter getStartingY() {
        return startingY;
    }

    public void setStartingY(final DoubleParameter startingY) {
        this.startingY = startingY;
    }

    public DoubleParameter getDiameter() {
        return diameter;
    }

    public void setDiameter(final DoubleParameter diameter) {
        this.diameter = diameter;
    }

    public DoubleParameter getSpeedInDays() {
        return speedInDays;
    }

    public void setSpeedInDays(final DoubleParameter speedInDays) {
        this.speedInDays = speedInDays;
    }

    public DoubleParameter getBiomassEach() {
        return biomassEach;
    }

    public void setBiomassEach(final DoubleParameter biomassEach) {
        this.biomassEach = biomassEach;
    }
}
