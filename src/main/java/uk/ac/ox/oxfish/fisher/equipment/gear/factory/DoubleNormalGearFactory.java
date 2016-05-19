package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.gear.HomogeneousAbundanceGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.DoubleNormalFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.FixedProportionFilter;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.RetentionAbundanceFilter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Gear that has DoubleNormal selectivity and logistic retention. The default values are for Shortspine
 * Created by carrknight on 5/17/16.
 */
public class DoubleNormalGearFactory implements HomogeneousGearFactory
{

    /**
     * the peak of the double normal
     */
    private DoubleParameter peak = new FixedDoubleParameter(23.53);

    private DoubleParameter top = new FixedDoubleParameter(-7);

    private DoubleParameter  ascWidth = new FixedDoubleParameter(3.77);

    private DoubleParameter  dscWidth = new FixedDoubleParameter(6.78);

    private Double  initialScaling = Double.NaN;

    private Double  finalScaling = Double.NaN;

    private DoubleParameter binMin = new FixedDoubleParameter(0);

    private DoubleParameter binMax= new FixedDoubleParameter(75);

    private DoubleParameter binWidth= new FixedDoubleParameter(1);

    /**
     * retention inflection parameter
     */
    private DoubleParameter retentionInflection = new FixedDoubleParameter(28.11);


    /**
     * retention slope parameter
     */
    private DoubleParameter retentionSlope = new FixedDoubleParameter(3.43);


    /**
     * retention slope parameter
     */
    private DoubleParameter retentionAsymptote = new FixedDoubleParameter(1);

    private DoubleParameter litersOfGasConsumedPerHour = new FixedDoubleParameter(0);

    private DoubleParameter averageCatchability = new FixedDoubleParameter(0);

    public DoubleNormalGearFactory()
    {


    }


    public DoubleNormalGearFactory(
            Double peak, Double top, Double ascWidth,
            Double dscWidth, Double initialScaling, Double finalScaling,
            Double binMin, Double binMax, Double binWidth,
            Double retentionInflection, Double retentionSlope,
            Double retentionAsymptote,
            Double litersOfGasConsumedPerHour,
            Double averageCatchability) {
        this.peak = new FixedDoubleParameter(peak);
        this.top = new FixedDoubleParameter(top);
        this.ascWidth = new FixedDoubleParameter(ascWidth);
        this.dscWidth = new FixedDoubleParameter(dscWidth);
        this.initialScaling = initialScaling;
        this.finalScaling = finalScaling;
        this.binMin = new FixedDoubleParameter(binMin);
        this.binMax = new FixedDoubleParameter(binMax);
        this.binWidth = new FixedDoubleParameter(binWidth);
        this.retentionInflection = new FixedDoubleParameter(retentionInflection);
        this.retentionSlope = new FixedDoubleParameter(retentionSlope);
        this.retentionAsymptote = new FixedDoubleParameter(retentionAsymptote);
        this.litersOfGasConsumedPerHour = new FixedDoubleParameter(litersOfGasConsumedPerHour);
        this.averageCatchability = new FixedDoubleParameter(averageCatchability);
    }

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HomogeneousAbundanceGear apply(FishState state) {
        MersenneTwisterFast random = state.getRandom();
        return new HomogeneousAbundanceGear(litersOfGasConsumedPerHour.apply(random),
                                            new FixedProportionFilter(averageCatchability.apply(random)),
                                            new DoubleNormalFilter(
                                                    true,
                                                    peak.apply(random),
                                                    top.apply(random),
                                                    ascWidth.apply(random),
                                                    dscWidth.apply(random),
                                                    initialScaling,finalScaling,
                                                    binMin.apply(random),
                                                    binMax.apply(random),
                                                    binWidth.apply(random)
                                            ),
                                            new RetentionAbundanceFilter(true,
                                                                         retentionInflection.apply(random),
                                                                         retentionSlope.apply(random),
                                                                         retentionAsymptote.apply(random))
        );
    }

    /**
     * Getter for property 'peak'.
     *
     * @return Value for property 'peak'.
     */
    public DoubleParameter getPeak() {
        return peak;
    }

    /**
     * Setter for property 'peak'.
     *
     * @param peak Value to set for property 'peak'.
     */
    public void setPeak(DoubleParameter peak) {
        this.peak = peak;
    }

    /**
     * Getter for property 'top'.
     *
     * @return Value for property 'top'.
     */
    public DoubleParameter getTop() {
        return top;
    }

    /**
     * Setter for property 'top'.
     *
     * @param top Value to set for property 'top'.
     */
    public void setTop(DoubleParameter top) {
        this.top = top;
    }

    /**
     * Getter for property 'ascWidth'.
     *
     * @return Value for property 'ascWidth'.
     */
    public DoubleParameter getAscWidth() {
        return ascWidth;
    }

    /**
     * Setter for property 'ascWidth'.
     *
     * @param ascWidth Value to set for property 'ascWidth'.
     */
    public void setAscWidth(DoubleParameter ascWidth) {
        this.ascWidth = ascWidth;
    }

    /**
     * Getter for property 'dscWidth'.
     *
     * @return Value for property 'dscWidth'.
     */
    public DoubleParameter getDscWidth() {
        return dscWidth;
    }

    /**
     * Setter for property 'dscWidth'.
     *
     * @param dscWidth Value to set for property 'dscWidth'.
     */
    public void setDscWidth(DoubleParameter dscWidth) {
        this.dscWidth = dscWidth;
    }

    /**
     * Getter for property 'initialScaling'.
     *
     * @return Value for property 'initialScaling'.
     */
    public Double getInitialScaling() {
        return initialScaling;
    }

    /**
     * Setter for property 'initialScaling'.
     *
     * @param initialScaling Value to set for property 'initialScaling'.
     */
    public void setInitialScaling(Double initialScaling) {
        this.initialScaling = initialScaling;
    }

    /**
     * Getter for property 'finalScaling'.
     *
     * @return Value for property 'finalScaling'.
     */
    public Double getFinalScaling() {
        return finalScaling;
    }

    /**
     * Setter for property 'finalScaling'.
     *
     * @param finalScaling Value to set for property 'finalScaling'.
     */
    public void setFinalScaling(Double finalScaling) {
        this.finalScaling = finalScaling;
    }

    /**
     * Getter for property 'binMin'.
     *
     * @return Value for property 'binMin'.
     */
    public DoubleParameter getBinMin() {
        return binMin;
    }

    /**
     * Setter for property 'binMin'.
     *
     * @param binMin Value to set for property 'binMin'.
     */
    public void setBinMin(DoubleParameter binMin) {
        this.binMin = binMin;
    }

    /**
     * Getter for property 'binMax'.
     *
     * @return Value for property 'binMax'.
     */
    public DoubleParameter getBinMax() {
        return binMax;
    }

    /**
     * Setter for property 'binMax'.
     *
     * @param binMax Value to set for property 'binMax'.
     */
    public void setBinMax(DoubleParameter binMax) {
        this.binMax = binMax;
    }

    /**
     * Getter for property 'binWidth'.
     *
     * @return Value for property 'binWidth'.
     */
    public DoubleParameter getBinWidth() {
        return binWidth;
    }

    /**
     * Setter for property 'binWidth'.
     *
     * @param binWidth Value to set for property 'binWidth'.
     */
    public void setBinWidth(DoubleParameter binWidth) {
        this.binWidth = binWidth;
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

    /** {@inheritDoc} */
    @Override
    public DoubleParameter getAverageCatchability() {
        return averageCatchability;
    }

    /** {@inheritDoc} */
    @Override
    public void setAverageCatchability(DoubleParameter averageCatchability) {
        this.averageCatchability = averageCatchability;
    }
}
