package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.HabitatAwareRandomCatchability;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates HabitatAwareRandomCatchability gears
 * Created by carrknight on 9/30/15.
 */
public class HabitatAwareGearFactory  implements AlgorithmFactory<HabitatAwareRandomCatchability>
{


    private DoubleParameter meanCatchabilityRocky = new FixedDoubleParameter(.01);

    private DoubleParameter standardDeviationCatchabilityRocky = new FixedDoubleParameter(0);


    private DoubleParameter meanCatchabilitySandy = new FixedDoubleParameter(.01);

    private DoubleParameter standardDeviationCatchabilitySandy = new FixedDoubleParameter(0);

    private DoubleParameter trawlSpeed = new FixedDoubleParameter(5);


    public HabitatAwareGearFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public HabitatAwareRandomCatchability apply(FishState state) {


        int species = state.getSpecies().size();
        double[] meansRocky = new double[species];
        double[] meansSandy = new double[species];
        double[] stdRocky = new double[species];
        double[] stdSandy = new double[species];


        for(int i=0; i<meansSandy.length; i++)
        {
            meansRocky[i] = meanCatchabilityRocky.apply(state.getRandom());
            meansSandy[i] = meanCatchabilitySandy.apply(state.getRandom());
            stdSandy[i] = standardDeviationCatchabilitySandy.apply(state.getRandom());
            stdRocky[i] = standardDeviationCatchabilityRocky.apply(state.getRandom());
        }

        return new HabitatAwareRandomCatchability(meansSandy,stdSandy,meansRocky,stdRocky,
                                                  trawlSpeed.apply(state.getRandom()));

    }

    public DoubleParameter getMeanCatchabilityRocky() {
        return meanCatchabilityRocky;
    }

    public void setMeanCatchabilityRocky(DoubleParameter meanCatchabilityRocky) {
        this.meanCatchabilityRocky = meanCatchabilityRocky;
    }

    public DoubleParameter getStandardDeviationCatchabilityRocky() {
        return standardDeviationCatchabilityRocky;
    }

    public void setStandardDeviationCatchabilityRocky(
            DoubleParameter standardDeviationCatchabilityRocky) {
        this.standardDeviationCatchabilityRocky = standardDeviationCatchabilityRocky;
    }

    public DoubleParameter getMeanCatchabilitySandy() {
        return meanCatchabilitySandy;
    }

    public void setMeanCatchabilitySandy(DoubleParameter meanCatchabilitySandy) {
        this.meanCatchabilitySandy = meanCatchabilitySandy;
    }

    public DoubleParameter getStandardDeviationCatchabilitySandy() {
        return standardDeviationCatchabilitySandy;
    }

    public void setStandardDeviationCatchabilitySandy(
            DoubleParameter standardDeviationCatchabilitySandy) {
        this.standardDeviationCatchabilitySandy = standardDeviationCatchabilitySandy;
    }

    public DoubleParameter getTrawlSpeed() {
        return trawlSpeed;
    }

    public void setTrawlSpeed(DoubleParameter trawlSpeed) {
        this.trawlSpeed = trawlSpeed;
    }
}
