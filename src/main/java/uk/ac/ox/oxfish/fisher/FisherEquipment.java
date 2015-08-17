package uk.ac.ox.oxfish.fisher;

import uk.ac.ox.oxfish.biology.Specie;
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


    public FisherEquipment(Boat boat, Hold hold, Gear gear) {
        this.boat = boat;
        this.hold = hold;
        this.gear = gear;
    }

    public Hold getHold() {
        return hold;
    }

    public void setHold(Hold hold) {
        this.hold = hold;
    }

    public Boat getBoat() {
        return boat;
    }

    public void setBoat(Boat boat) {
        this.boat = boat;
    }

    public Gear getGear() {
        return gear;
    }

    public void setGear(Gear gear) {
        this.gear = gear;
    }

    public double getTotalPoundsCarried() {
        return hold.getTotalPoundsCarried();
    }

    public double getPoundsCarried(Specie specie) {
        return hold.getPoundsCarried(specie);
    }

    public double getMaximumLoad() {
        return hold.getMaximumLoad();
    }

    public double getPercentageFilled() {
        return hold.getPercentageFilled();
    }


    public FisherEquipment makeCopy()
    {
        FuelTank tank = new FuelTank(boat.getFuelCapacityInLiters());
        tank.refill();
        tank.consume(tank.getFuelCapacityInLiters()-boat.getLitersOfFuelInTank());
        return new FisherEquipment(new Boat(boat.getLength(),boat.getWidth(),new Engine(boat.getWeightInKg(),boat.getEfficiencyAsLitersPerKm(),boat.getSpeedInKph()),
                                            tank),hold.makeCopy(),gear.makeCopy());
    }
}