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
     * the length of the boat, in meters
     */
    private final double length;

    /**
     * the width of the boat, in meters
     */
    private final double width;


    /**
     * the engine: holding speed and weight and efficiency
     */
    private Engine engine;

    /**
     * fuel counter
     */
    private FuelTank tank;


    /**
     * how many hours have been spent travelling in this step
     */
    private double hoursTravelledToday = 0;

    public Boat(double length, double width, Engine engine, FuelTank tank) {
        Preconditions.checkArgument(length > 0, "length must be positive > 0");
        Preconditions.checkArgument(width > 0, "width must be positive > 0");

        this.length = FishStateUtilities.round(length);
        this.width = FishStateUtilities.round(width);
        this.engine = engine;
        this.tank = tank;
    }

    /**
     * tell the boat a new day has arrived (and therefore the hoursTravelledToday can be reset)
     */
    public void newStep() {
        hoursTravelledToday = 0;
    }

    /**
     * like hypotheticalTravelTimeToMoveThisMuchAtFullSpeed but adds to it the hours this boat has already travelled
     *
     * @param segmentLengthInKilometers the length of the new step
     * @return current travel time + travel time of the new segment (in hours)
     */
    public double totalTravelTimeAfterAddingThisSegment(double segmentLengthInKilometers) {

        return hoursTravelledToday + hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(segmentLengthInKilometers);
    }

    /**
     * how much time it takes to travel this many kilometers
     *
     * @param kilometersToTravel how many kilometers to move through
     * @return how many hours it takes to move "kilometersToTravel" (in hours)
     */
    public double hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(double kilometersToTravel) {
        //  Preconditions.checkArgument(kilometersToTravel > 0);
        return kilometersToTravel / engine.getSpeedInKph();
    }

    public Engine getEngine() {
        return engine;
    }

    /**
     * Setter for property 'engine'.
     *
     * @param engine Value to set for property 'engine'.
     */
    public void setEngine(Engine engine) {
        this.engine = engine;

    }

    /**
     * adds the hours spent travelling to the hoursTravelledToday
     *
     * @param distanceTravelled kilometers travelled
     */
    public void recordTravel(double distanceTravelled) {

        hoursTravelledToday += hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(distanceTravelled);

    }

    public double getHoursTravelledToday() {
        return hoursTravelledToday;
    }

    /**
     * Setter for property 'hoursTravelledToday'.
     *
     * @param hoursTravelledToday Value to set for property 'hoursTravelledToday'.
     */
    public void setHoursTravelledToday(double hoursTravelledToday) {
        this.hoursTravelledToday = hoursTravelledToday;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getFuelCapacityInLiters() {
        return tank.getFuelCapacityInLiters();
    }

    public void consumeFuel(double litersOfGasConsumed) {
        tank.consume(litersOfGasConsumed);
    }

    /**
     * is there enough fuel in the tank for the trip home?
     *
     * @param lengthInKm length of the trip in kilometer
     * @param margin     margin of error. If 1, it just checks if there is just enough fuel in the tank to make the trip.
     *                   1.05 would mean that it returns false if there is just enough fuel to make the trip but not 5% more.
     *                   Anything less than 1 throws an exception
     * @return true if there is enough fuel in the tank to travel trip*margin kilometers
     */
    public boolean isFuelEnoughForTrip(double lengthInKm, double margin) {

        Preconditions.checkArgument(margin >= 1);
        return expectedFuelConsumption(lengthInKm) * margin <= tank.getLitersOfFuelInTank();

    }

    /**
     * liters of gas consumed for travelling that distance
     *
     * @param kmTravelled distance travelled
     * @return liters of gas
     */
    public double expectedFuelConsumption(double kmTravelled) {
        return engine.getGasConsumptionPerKm(kmTravelled);

    }

    /**
     * fill the tank to the brim.
     *
     * @return how much gas had to be put in
     */
    public double refill() {
        return tank.refill();
    }

    public double getLitersOfFuelInTank() {
        return tank.getLitersOfFuelInTank();
    }

    public double getPowerInBhp() {
        return engine.getPowerInBhp();
    }

    public double getSpeedInKph() {
        return engine.getSpeedInKph();
    }

    public double getEfficiencyAsLitersPerKm() {
        return engine.getEfficiencyAsLitersPerKm();
    }
}
