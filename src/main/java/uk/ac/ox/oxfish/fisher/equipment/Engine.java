package uk.ac.ox.oxfish.fisher.equipment;

/**
 * A simple object/struct holding the weight of the engine and its fuel efficency plus some
 * utility methods to compute gas consumption
 * Created by carrknight on 7/15/15.
 */
public class Engine
{

    /**
     * the power of the engine in bhp. Unused.
     */
    private final double powerInBhp;

    /**
     * the efficency of the engine as how many liters of gas are consumed for each kilometer travelled
     */
    private final double efficiencyAsLitersPerKm;


    /**
     * speed of the boat in knots
     */
    private final double speedInKph;


    public Engine(double powerInBhp, double efficiencyAsLitersPerKm, double speedInKph) {
        this.powerInBhp = powerInBhp;
        this.efficiencyAsLitersPerKm = efficiencyAsLitersPerKm;
        this.speedInKph = speedInKph;
    }

    public double getPowerInBhp() {
        return powerInBhp;
    }

    public double getEfficiencyAsLitersPerKm() {
        return efficiencyAsLitersPerKm;
    }


    public double getGasConsumptionPerKm(double KmTravelled)
    {
        return efficiencyAsLitersPerKm *KmTravelled;
    }

    public double getSpeedInKph() {
        return speedInKph;
    }
}
