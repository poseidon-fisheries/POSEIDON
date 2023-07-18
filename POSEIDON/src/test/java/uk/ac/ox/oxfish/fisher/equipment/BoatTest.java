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

package uk.ac.ox.oxfish.fisher.equipment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class BoatTest {


    @Test
    public void storesCorrectly() throws Exception {

        Boat slowBoat = new Boat(1, 1, new Engine(5.0, 5, 5), new FuelTank(100000));
        Assertions.assertEquals(2, slowBoat.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(10), .001); // 10 kilometers in 2 hours
        Assertions.assertEquals(2, slowBoat.totalTravelTimeAfterAddingThisSegment(10), .001); // 10 kilometers in 2 hours
        slowBoat.recordTravel(10);
        Assertions.assertEquals(2, slowBoat.hypotheticalTravelTimeToMoveThisMuchAtFullSpeed(10), .001); // again
        Assertions.assertEquals(4, slowBoat.totalTravelTimeAfterAddingThisSegment(10), .001); // 4 hours total
        Assertions.assertEquals(2, slowBoat.getHoursTravelledToday(), .001);

        slowBoat.recordTravel(2.5); //this should be another half an hour
        Assertions.assertEquals(2.5, slowBoat.getHoursTravelledToday(), .001);


    }


    @Test
    public void fuelIsEnough() throws Exception {

        Engine engine = new Engine(100, 10, 1); //10 liters per kilometer
        FuelTank tank = new FuelTank(1000); //1000 liters
        Boat boat = new Boat(1, 1, engine, tank);

        //you have enough for 50 km
        Assertions.assertTrue(boat.isFuelEnoughForTrip(50, 1));
        Assertions.assertTrue(boat.isFuelEnoughForTrip(50, 1.05));
        //you have JUST enough for 100km
        Assertions.assertTrue(boat.isFuelEnoughForTrip(100, 1));
        Assertions.assertFalse(boat.isFuelEnoughForTrip(100, 1.01));
        //not enough for 101km though
        Assertions.assertFalse(boat.isFuelEnoughForTrip(101, 1));

        //consume a bit
        boat.consumeFuel(800);
        //now it's not enough for 50km either
        Assertions.assertFalse(boat.isFuelEnoughForTrip(50, 1));

        //refill, and we are back
        boat.refill();
        Assertions.assertTrue(boat.isFuelEnoughForTrip(50, 1));
        Assertions.assertTrue(boat.isFuelEnoughForTrip(50, 1.05));
        Assertions.assertTrue(boat.isFuelEnoughForTrip(100, 1));
        Assertions.assertFalse(boat.isFuelEnoughForTrip(100, 1.01));
        Assertions.assertFalse(boat.isFuelEnoughForTrip(101, 1));

    }
}