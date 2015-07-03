package uk.ac.ox.oxfish.fisher.equipment;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Holds information about the speed of the fisher (later it will probably have gas/efficiency and other non-fishing
 * related information)
 * Created by carrknight on 4/18/15.
 */
public class Boat {

    /**
     * speed of the boat in knots
     */
    private final double boatSpeedInKph;


    /**
     * the length of the boat, in meters
     */
    private final double boatLenght;

    /**
     * the width of the boat, in meters
     */
    private final double boatWidth;



    /**
     * how many hours have been spent travelling in this step
     */
    private double hoursTravelledToday = 0;

    public Boat(double boatSpeedInKph, double boatLenght, double boatWidth) {
        Preconditions.checkArgument(boatSpeedInKph > 0, "speed must be positive > 0");
        Preconditions.checkArgument(boatLenght > 0, "length must be positive > 0");
        Preconditions.checkArgument(boatWidth > 0, "width must be positive > 0");

        this.boatSpeedInKph = boatSpeedInKph;
        this.boatLenght = FishStateUtilities.round(boatLenght);
        this.boatWidth =  FishStateUtilities.round(boatWidth);
    }

    /**
     * tell the boat a new day has arrived (and therefore the hoursTravelledToday can be reset)
     */
    public void newStep()
    {
        hoursTravelledToday = 0;
    }

    /**
     * how much time it takes to travel this many kilometers
     * @param kilometersToTravel how many kilometers to move through
     * @return how many hours it takes to move "kilometersToTravel" (in hours)
     */
    public double hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(double kilometersToTravel)
    {
        Preconditions.checkArgument(kilometersToTravel > 0);
        return kilometersToTravel/ boatSpeedInKph;
    }


    /**
     * like hypotheticalTravelTimeToMoveThisMuchAtFullSpeed but adds to it the hours this boat has already travelled
     * @param segmentLengthInKilometers the length of the new step
     * @return current travel time + travel time of the new segment (in hours)
     */
    public double totalTravelTimeAfterAddingThisSegment(double segmentLengthInKilometers){

        return hoursTravelledToday + hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(segmentLengthInKilometers);
    }


    /**
     * adds the hours spent travelling to the hoursTravelledToday
     * @param distanceTravelled kilometers travelled
     */
    public void recordTravel(double distanceTravelled)
    {

        hoursTravelledToday += hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(distanceTravelled);

    }


    public double getHoursTravelledToday() {
        return hoursTravelledToday;
    }

    public double getBoatLenght() {
        return boatLenght;
    }

    public double getBoatWidth() {
        return boatWidth;
    }
}
