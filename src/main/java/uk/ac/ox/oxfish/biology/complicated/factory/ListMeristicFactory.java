package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Created by carrknight on 7/7/17.
 */
public class ListMeristicFactory implements AlgorithmFactory<FromListMeristics>{

    /**
     * gets turned into a list of doubles
     */
    private String weightsPerBin = "10,50,100";


    private DoubleParameter mortalityRate = new FixedDoubleParameter(0.08);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public FromListMeristics apply(FishState fishState) {


        //turn into weights
        Double[] weights = Arrays.stream(weightsPerBin.split(",")).map(new Function<String, Double>() {
            @Override
            public Double apply(String s) {
                return Double.parseDouble(s.trim());
            }
        }).toArray(Double[]::new);

        //create a meristic!
        return new FromListMeristics(mortalityRate.apply(fishState.getRandom()),
                                     weights);





    }

    /**
     * Getter for property 'weightsPerBin'.
     *
     * @return Value for property 'weightsPerBin'.
     */
    public String getWeightsPerBin() {
        return weightsPerBin;
    }

    /**
     * Setter for property 'weightsPerBin'.
     *
     * @param weightsPerBin Value to set for property 'weightsPerBin'.
     */
    public void setWeightsPerBin(String weightsPerBin) {
        this.weightsPerBin = weightsPerBin;
    }

    /**
     * Getter for property 'mortalityRate'.
     *
     * @return Value for property 'mortalityRate'.
     */
    public DoubleParameter getMortalityRate() {
        return mortalityRate;
    }

    /**
     * Setter for property 'mortalityRate'.
     *
     * @param mortalityRate Value to set for property 'mortalityRate'.
     */
    public void setMortalityRate(DoubleParameter mortalityRate) {
        this.mortalityRate = mortalityRate;
    }
}
