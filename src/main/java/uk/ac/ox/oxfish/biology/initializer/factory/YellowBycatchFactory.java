package uk.ac.ox.oxfish.biology.initializer.factory;

import uk.ac.ox.oxfish.biology.initializer.YellowBycatchInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 1/21/17.
 */
public class YellowBycatchFactory implements AlgorithmFactory<YellowBycatchInitializer>
{
    private boolean separateYelloweyeStock = false;

    private String targetSpeciesName = "Sablefish";

    private String bycatchSpeciesName  = "Yelloweye Rockfish";

    private DoubleParameter initialTargetAbundance
            = new FixedDoubleParameter(100000000);

    private DoubleParameter initialBycatchAbundance
            = new FixedDoubleParameter(10000000);

    private DoubleParameter proportionJuvenileTarget
            = new FixedDoubleParameter(.2);

    private DoubleParameter proportionJuvenileBycatch
            = new FixedDoubleParameter(.2);


    /**
     * any cell with x >= verticalSeparator will include the bycatch species
     */
    private DoubleParameter verticalSeparator
            = new FixedDoubleParameter(25);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public YellowBycatchInitializer apply(FishState state) {
        return new YellowBycatchInitializer(
                separateYelloweyeStock,
                targetSpeciesName,
                bycatchSpeciesName,
                initialTargetAbundance.apply(state.getRandom()).intValue(),
                initialBycatchAbundance.apply(state.getRandom()).intValue(),
                proportionJuvenileTarget.apply(state.getRandom()),
                proportionJuvenileBycatch.apply(state.getRandom())

        );
    }

    /**
     * Getter for property 'separateYelloweyeStock'.
     *
     * @return Value for property 'separateYelloweyeStock'.
     */
    public boolean isSeparateYelloweyeStock() {
        return separateYelloweyeStock;
    }

    /**
     * Setter for property 'separateYelloweyeStock'.
     *
     * @param separateYelloweyeStock Value to set for property 'separateYelloweyeStock'.
     */
    public void setSeparateYelloweyeStock(boolean separateYelloweyeStock) {
        this.separateYelloweyeStock = separateYelloweyeStock;
    }

    /**
     * Getter for property 'targetSpeciesName'.
     *
     * @return Value for property 'targetSpeciesName'.
     */
    public String getTargetSpeciesName() {
        return targetSpeciesName;
    }

    /**
     * Setter for property 'targetSpeciesName'.
     *
     * @param targetSpeciesName Value to set for property 'targetSpeciesName'.
     */
    public void setTargetSpeciesName(String targetSpeciesName) {
        this.targetSpeciesName = targetSpeciesName;
    }

    /**
     * Getter for property 'bycatchSpeciesName'.
     *
     * @return Value for property 'bycatchSpeciesName'.
     */
    public String getBycatchSpeciesName() {
        return bycatchSpeciesName;
    }

    /**
     * Setter for property 'bycatchSpeciesName'.
     *
     * @param bycatchSpeciesName Value to set for property 'bycatchSpeciesName'.
     */
    public void setBycatchSpeciesName(String bycatchSpeciesName) {
        this.bycatchSpeciesName = bycatchSpeciesName;
    }

    /**
     * Getter for property 'initialTargetAbundance'.
     *
     * @return Value for property 'initialTargetAbundance'.
     */
    public DoubleParameter getInitialTargetAbundance() {
        return initialTargetAbundance;
    }

    /**
     * Setter for property 'initialTargetAbundance'.
     *
     * @param initialTargetAbundance Value to set for property 'initialTargetAbundance'.
     */
    public void setInitialTargetAbundance(DoubleParameter initialTargetAbundance) {
        this.initialTargetAbundance = initialTargetAbundance;
    }

    /**
     * Getter for property 'initialBycatchAbundance'.
     *
     * @return Value for property 'initialBycatchAbundance'.
     */
    public DoubleParameter getInitialBycatchAbundance() {
        return initialBycatchAbundance;
    }

    /**
     * Setter for property 'initialBycatchAbundance'.
     *
     * @param initialBycatchAbundance Value to set for property 'initialBycatchAbundance'.
     */
    public void setInitialBycatchAbundance(DoubleParameter initialBycatchAbundance) {
        this.initialBycatchAbundance = initialBycatchAbundance;
    }

    /**
     * Getter for property 'proportionJuvenileTarget'.
     *
     * @return Value for property 'proportionJuvenileTarget'.
     */
    public DoubleParameter getProportionJuvenileTarget() {
        return proportionJuvenileTarget;
    }

    /**
     * Setter for property 'proportionJuvenileTarget'.
     *
     * @param proportionJuvenileTarget Value to set for property 'proportionJuvenileTarget'.
     */
    public void setProportionJuvenileTarget(DoubleParameter proportionJuvenileTarget) {
        this.proportionJuvenileTarget = proportionJuvenileTarget;
    }

    /**
     * Getter for property 'proportionJuvenileBycatch'.
     *
     * @return Value for property 'proportionJuvenileBycatch'.
     */
    public DoubleParameter getProportionJuvenileBycatch() {
        return proportionJuvenileBycatch;
    }

    /**
     * Setter for property 'proportionJuvenileBycatch'.
     *
     * @param proportionJuvenileBycatch Value to set for property 'proportionJuvenileBycatch'.
     */
    public void setProportionJuvenileBycatch(DoubleParameter proportionJuvenileBycatch) {
        this.proportionJuvenileBycatch = proportionJuvenileBycatch;
    }

    /**
     * Getter for property 'verticalSeparator'.
     *
     * @return Value for property 'verticalSeparator'.
     */
    public DoubleParameter getVerticalSeparator() {
        return verticalSeparator;
    }

    /**
     * Setter for property 'verticalSeparator'.
     *
     * @param verticalSeparator Value to set for property 'verticalSeparator'.
     */
    public void setVerticalSeparator(DoubleParameter verticalSeparator) {
        this.verticalSeparator = verticalSeparator;
    }
}
