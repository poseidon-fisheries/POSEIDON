package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.SelectivityAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticAbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticSimpleFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class SimpleLogisticGearFactory implements HomogeneousGearFactory {


    private boolean rounding = false;
    /**
     * the selectivity parameter A for the logistic
     */
    private DoubleParameter selexParameter1 = new FixedDoubleParameter(15.0948823);

    /**
     * the selectivity parameter B for the logistic
     */
    private DoubleParameter selexParameter2 = new FixedDoubleParameter(0.5391);


    private DoubleParameter litersOfGasConsumedPerHour = new FixedDoubleParameter(0);

    private DoubleParameter averageCatchability = new FixedDoubleParameter(0);




    @Override
    public HomogeneousAbundanceGear apply(FishState fishState) {

        MersenneTwisterFast random = fishState.getRandom();

        return new HomogeneousAbundanceGear(
                litersOfGasConsumedPerHour.apply(random),
                new FixedProportionFilter(averageCatchability.apply(random), rounding),
                new LogisticSimpleFilter(
                        true,
                        rounding,
                        selexParameter1.apply(random),
                        selexParameter2.apply(random)));
    }

    public boolean isRounding() {
        return rounding;
    }

    public void setRounding(boolean rounding) {
        this.rounding = rounding;
    }

    public DoubleParameter getSelexParameter1() {
        return selexParameter1;
    }

    public void setSelexParameter1(DoubleParameter selexParameter1) {
        this.selexParameter1 = selexParameter1;
    }

    public DoubleParameter getSelexParameter2() {
        return selexParameter2;
    }

    public void setSelexParameter2(DoubleParameter selexParameter2) {
        this.selexParameter2 = selexParameter2;
    }


    public DoubleParameter getLitersOfGasConsumedPerHour() {
        return litersOfGasConsumedPerHour;
    }

    public void setLitersOfGasConsumedPerHour(DoubleParameter litersOfGasConsumedPerHour) {
        this.litersOfGasConsumedPerHour = litersOfGasConsumedPerHour;
    }

    @Override
    public DoubleParameter getAverageCatchability() {
        return averageCatchability;
    }

    @Override
    public void setAverageCatchability(DoubleParameter averageCatchability) {
        this.averageCatchability = averageCatchability;
    }
}
