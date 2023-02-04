package uk.ac.ox.oxfish.model.regs.policymakers;

import com.google.common.base.MoreObjects;

public class SurplusProductionResult {


    private final double carryingCapacity;

    private final double logisticGrowth;

    private final double catchability;

    private final double[] depletion;

    private final double[] cpue;

    private final double[] landings;

    private final double[] biomass;


    public SurplusProductionResult(double carryingCapacity,
                                   double logisticGrowth,
                                   double catchability, double[] depletion,
                                   double[] cpue, double[] landings, double[] biomass) {
        this.carryingCapacity = carryingCapacity;
        this.logisticGrowth = logisticGrowth;
        this.catchability = catchability;
        this.depletion = depletion;
        this.cpue = cpue;
        this.landings = landings;
        this.biomass = biomass;
    }

    public double getCarryingCapacity() {
        return carryingCapacity;
    }

    public double[] getDepletion() {
        return depletion;
    }

    public double[] getCpue() {
        return cpue;
    }

    public double[] getLandings() {
        return landings;
    }

    public double[] getBiomass() {
        return biomass;
    }

    public double getLogisticGrowth() {
        return logisticGrowth;
    }

    public double getCatchability() {
        return catchability;
    }


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("carryingCapacity", carryingCapacity)
                .add("logisticGrowth", logisticGrowth)
                .add("catchability", catchability)
                .add("depletion", depletion)
                .add("cpue", cpue)
                .add("landings", landings)
                .add("biomass", biomass)
                .toString();
    }
}
