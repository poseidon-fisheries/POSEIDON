package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.LogisticAbundanceFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.RetentionAbundanceFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * This is a gear that applies the same Logistic selectivity and Logistic Retention to every species.
 * The default numbers refer to the thornyhead gear
 * Created by carrknight on 3/18/16.
 */
public class LogisticSelectivityGearFactory implements AlgorithmFactory<HomogeneousAbundanceGear> {


    /**
     * the selectivity parameter A for the logistic
     */
    private DoubleParameter selectivityAParameter = new FixedDoubleParameter(23.5035);

    /**
     * the selectivity parameter B for the logistic
     */
    private DoubleParameter selectivityBParameter = new FixedDoubleParameter(9.03702);

    /**
     * retention inflection parameter
     */
    private DoubleParameter retentionInflection = new FixedDoubleParameter(21.8035);


 /**
     * retention slope parameter
     */
    private DoubleParameter retentionSlope = new FixedDoubleParameter(1.7773);


    /**
     * retention slope parameter
     */
    private DoubleParameter retentionAsymptote = new FixedDoubleParameter(0.992661);


    private DoubleParameter litersOfGasConsumedPerHour = new FixedDoubleParameter(0);

    private DoubleParameter averageCatchability = new FixedDoubleParameter(0);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public HomogeneousAbundanceGear apply(FishState fishState) {
        MersenneTwisterFast random = fishState.getRandom();
        return new HomogeneousAbundanceGear(litersOfGasConsumedPerHour.apply(random),
                                            new FixedProportionFilter(averageCatchability.apply(random)),
                                            new LogisticAbundanceFilter(selectivityAParameter.apply(random),
                                                                        selectivityBParameter.apply(random),
                                                                        true),
                                            new RetentionAbundanceFilter(true,
                                                                         retentionInflection.apply(random),
                                                                         retentionSlope.apply(random),
                                                                         retentionAsymptote.apply(random))
                                            );
    }


    /**
     * Getter for property 'selectivityAParameter'.
     *
     * @return Value for property 'selectivityAParameter'.
     */
    public DoubleParameter getSelectivityAParameter() {
        return selectivityAParameter;
    }

    /**
     * Setter for property 'selectivityAParameter'.
     *
     * @param selectivityAParameter Value to set for property 'selectivityAParameter'.
     */
    public void setSelectivityAParameter(DoubleParameter selectivityAParameter) {
        this.selectivityAParameter = selectivityAParameter;
    }

    /**
     * Getter for property 'selectivityBParameter'.
     *
     * @return Value for property 'selectivityBParameter'.
     */
    public DoubleParameter getSelectivityBParameter() {
        return selectivityBParameter;
    }

    /**
     * Setter for property 'selectivityBParameter'.
     *
     * @param selectivityBParameter Value to set for property 'selectivityBParameter'.
     */
    public void setSelectivityBParameter(DoubleParameter selectivityBParameter) {
        this.selectivityBParameter = selectivityBParameter;
    }

    /**
     * Getter for property 'retentionInflection'.
     *
     * @return Value for property 'retentionInflection'.
     */
    public DoubleParameter getRetentionInflection() {
        return retentionInflection;
    }

    /**
     * Setter for property 'retentionInflection'.
     *
     * @param retentionInflection Value to set for property 'retentionInflection'.
     */
    public void setRetentionInflection(DoubleParameter retentionInflection) {
        this.retentionInflection = retentionInflection;
    }

    /**
     * Getter for property 'retentionSlope'.
     *
     * @return Value for property 'retentionSlope'.
     */
    public DoubleParameter getRetentionSlope() {
        return retentionSlope;
    }

    /**
     * Setter for property 'retentionSlope'.
     *
     * @param retentionSlope Value to set for property 'retentionSlope'.
     */
    public void setRetentionSlope(DoubleParameter retentionSlope) {
        this.retentionSlope = retentionSlope;
    }

    /**
     * Getter for property 'retentionAsymptote'.
     *
     * @return Value for property 'retentionAsymptote'.
     */
    public DoubleParameter getRetentionAsymptote() {
        return retentionAsymptote;
    }

    /**
     * Setter for property 'retentionAsymptote'.
     *
     * @param retentionAsymptote Value to set for property 'retentionAsymptote'.
     */
    public void setRetentionAsymptote(DoubleParameter retentionAsymptote) {
        this.retentionAsymptote = retentionAsymptote;
    }

    /**
     * Getter for property 'litersOfGasConsumedPerHour'.
     *
     * @return Value for property 'litersOfGasConsumedPerHour'.
     */
    public DoubleParameter getLitersOfGasConsumedPerHour() {
        return litersOfGasConsumedPerHour;
    }

    /**
     * Setter for property 'litersOfGasConsumedPerHour'.
     *
     * @param litersOfGasConsumedPerHour Value to set for property 'litersOfGasConsumedPerHour'.
     */
    public void setLitersOfGasConsumedPerHour(
            DoubleParameter litersOfGasConsumedPerHour) {
        this.litersOfGasConsumedPerHour = litersOfGasConsumedPerHour;
    }

    /**
     * Getter for property 'averageCatchability'.
     *
     * @return Value for property 'averageCatchability'.
     */
    public DoubleParameter getAverageCatchability() {
        return averageCatchability;
    }

    /**
     * Setter for property 'averageCatchability'.
     *
     * @param averageCatchability Value to set for property 'averageCatchability'.
     */
    public void setAverageCatchability(DoubleParameter averageCatchability) {
        this.averageCatchability = averageCatchability;
    }
}
