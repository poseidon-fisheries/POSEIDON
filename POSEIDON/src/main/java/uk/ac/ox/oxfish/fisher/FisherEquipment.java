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

package uk.ac.ox.oxfish.fisher;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Engine;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;

import java.io.Serializable;


/**
 * a container for all the fisher equipment variables
 */
public class FisherEquipment implements Serializable {
    private static final long serialVersionUID = 7023995324809495505L;
    /**
     * boat statistics (also holds information about how much the boat has travelled so far)
     */
    private Boat boat;
    /**
     * basically the inventory of the ship
     */
    private Hold hold;


    /**
     * what is used for fishing
     */
    private Gear gear;


    public FisherEquipment(final Boat boat, final Hold hold, final Gear gear) {
        this.boat = boat;
        this.hold = hold;
        this.gear = gear;
    }

    public Hold getHold() {
        return hold;
    }

    public void setHold(final Hold hold) {
        this.hold = hold;
    }

    public Boat getBoat() {
        return boat;
    }

    public void setBoat(final Boat boat) {
        this.boat = boat;
    }

    public Gear getGear() {
        return gear;
    }

    public void setGear(final Gear gear) {
        this.gear = gear;
    }

    public double getTotalWeightOfCatchInHold() {
        return hold.getTotalWeightOfCatchInHold();
    }

    public double getWeightOfCatchInHold(final Species species) {
        return hold.getWeightOfCatchInHold(species);
    }

    public double getMaximumLoad() {
        return hold.getMaximumLoad();
    }

    public double getPercentageFilled() {
        return hold.getPercentageFilled();
    }


    public FisherEquipment makeCopy() {
        final FuelTank tank = new FuelTank(boat.getFuelCapacityInLiters());
        tank.refill();
        tank.consume(tank.getFuelCapacityInLiters() - boat.getLitersOfFuelInTank());
        return new FisherEquipment(new Boat(
            boat.getLength(),
            boat.getWidth(),
            new Engine(boat.getPowerInBhp(), boat.getEfficiencyAsLitersPerKm(), boat.getSpeedInKph()),
            tank
        ), hold.makeCopy(), gear.makeCopy());
    }
}