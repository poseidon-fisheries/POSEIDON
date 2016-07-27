package uk.ac.ox.oxfish.fisher.equipment;

/**
 *  Just a dedicated counter to keep track of the fuel left!
 * Created by carrknight on 7/15/15.
 */
public class FuelTank {


    /**
     * the total amount of fuel that can be loaded in this tank
     */
    final private double fuelCapacityInLiters;

    /**
     * how much fuel left in tank
     */
    private double  litersOfFuelInTank;



    public FuelTank(double fuelCapacityInLiters) {
        this.fuelCapacityInLiters = fuelCapacityInLiters;
        litersOfFuelInTank = fuelCapacityInLiters;
    }


    public double getFuelCapacityInLiters() {
        return fuelCapacityInLiters;
    }

    public double getLitersOfFuelInTank() {
        return litersOfFuelInTank;
    }


    /**
     * fill the tank to the brim.
     * @return how much gas had to be put in
     */
    public double refill()
    {
        double added = fuelCapacityInLiters - litersOfFuelInTank;
        assert  added >=0;
        litersOfFuelInTank = fuelCapacityInLiters;
        return added;
    }

    public void consume(double litersOfGasConsumed)
    {
        litersOfFuelInTank-=litersOfGasConsumed;
      //  litersOfFuelInTank = FishStateUtilities.round(litersOfFuelInTank);
    }
}
