package uk.ac.ox.oxfish.fisher.equipment;

/**
 * A simple object/struct holding the weight of the engine and its fuel efficency plus some
 * utility methods to compute gas consumption
 * Created by carrknight on 7/15/15.
 */
public class Engine
{

    /**
     * the weight of the engine in kg
     */
    private final double weightInKg;

    /**
     * the efficency of the engine as how many liters of gas are consumed for each kilometer travelled
     */
    private final double efficiencyAsLitersPerKm;


    /**
     * speed of the boat in knots
     */
    private final double speedInKph;


    public Engine(double weightInKg, double efficiencyAsLitersPerKm, double speedInKph) {
        this.weightInKg = weightInKg;
        this.efficiencyAsLitersPerKm = efficiencyAsLitersPerKm;
        this.speedInKph = speedInKph;
    }

    public double getWeightInKg() {
        return weightInKg;
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
