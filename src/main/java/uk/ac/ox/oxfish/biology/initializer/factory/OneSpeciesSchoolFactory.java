package uk.ac.ox.oxfish.biology.initializer.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.initializer.OneSpeciesInfiniteSchoolsInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.util.ArrayList;

/**
 * Creates the "chaser" biology with a school of fishers running around
 * Created by carrknight on 11/17/16.
 */
public class OneSpeciesSchoolFactory implements AlgorithmFactory<OneSpeciesInfiniteSchoolsInitializer>
{


    private DoubleParameter numberOfSchools = new FixedDoubleParameter(1d);

    private String waypoints = "0,0 : 40,0 :  40,40: 0,40";

    private DoubleParameter startingX = new UniformDoubleParameter(0,30);

    private DoubleParameter startingY = new UniformDoubleParameter(0,30);

    private DoubleParameter diameter = new FixedDoubleParameter(4);

    private DoubleParameter speedInDays = new FixedDoubleParameter(10);

    private DoubleParameter  biomassEach = new FixedDoubleParameter(10000);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public OneSpeciesInfiniteSchoolsInitializer apply(FishState state) {


        String[] splitString = waypoints.split(":");
        Pair<Integer,Integer>[] trueWayPoints = new Pair[splitString.length];
        Preconditions.checkArgument(trueWayPoints.length>=2);
        for(int i=0 ;i<trueWayPoints.length; i++)
        {
            String[] waypoint = splitString[i].split(",");
            assert waypoint.length == 2;
            trueWayPoints[i] = new Pair<>(
                    Integer.parseInt(waypoint[0].trim()),
                    Integer.parseInt(waypoint[1].trim())
            );
        }


        return new OneSpeciesInfiniteSchoolsInitializer(
                numberOfSchools.apply(state.getRandom()).intValue(),
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

    public void setNumberOfSchools(DoubleParameter numberOfSchools) {
        this.numberOfSchools = numberOfSchools;
    }

    public String getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(String waypoints) {
        this.waypoints = waypoints;
    }

    public DoubleParameter getStartingX() {
        return startingX;
    }

    public void setStartingX(DoubleParameter startingX) {
        this.startingX = startingX;
    }

    public DoubleParameter getStartingY() {
        return startingY;
    }

    public void setStartingY(DoubleParameter startingY) {
        this.startingY = startingY;
    }

    public DoubleParameter getDiameter() {
        return diameter;
    }

    public void setDiameter(DoubleParameter diameter) {
        this.diameter = diameter;
    }

    public DoubleParameter getSpeedInDays() {
        return speedInDays;
    }

    public void setSpeedInDays(DoubleParameter speedInDays) {
        this.speedInDays = speedInDays;
    }

    public DoubleParameter getBiomassEach() {
        return biomassEach;
    }

    public void setBiomassEach(DoubleParameter biomassEach) {
        this.biomassEach = biomassEach;
    }
}
