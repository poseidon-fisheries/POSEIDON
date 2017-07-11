package uk.ac.ox.oxfish.biology.complicated.factory;

import com.beust.jcommander.internal.Lists;
import uk.ac.ox.oxfish.biology.complicated.InitialAbundance;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * Created by carrknight on 7/11/17.
 */
public class InitialAbundanceFromStringFactory implements AlgorithmFactory<InitialAbundance> {


    private String fishPerBinPerSex ="10000000,1000000,10000";


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public InitialAbundance apply(FishState state)
    {

        //turn into weights array
        Function<String, Integer> mapper = new Function<String, Integer>() {
            @Override
            public Integer apply(String s) {
                return Integer.parseInt(s.trim());
            }
        };

        Integer[] fish = Arrays.stream(fishPerBinPerSex.split(",")).map(mapper).toArray(Integer[]::new);



        //set up initial abundance array
        int[][] abundance = new int[2][];
        abundance[0]=new int[fish.length];
        abundance[1]=new int[fish.length];
        //fill it up
        for(int bin=0; bin<fish.length; bin++)
        {
            abundance[0][bin] = fish[bin];
            abundance[1][bin] = fish[bin];
        }
        //return it
        return new InitialAbundance(abundance);

    }

    /**
     * Getter for property 'fishPerBinPerSex'.
     *
     * @return Value for property 'fishPerBinPerSex'.
     */
    public String getFishPerBinPerSex() {
        return fishPerBinPerSex;
    }

    /**
     * Setter for property 'fishPerBinPerSex'.
     *
     * @param fishPerBinPerSex Value to set for property 'fishPerBinPerSex'.
     */
    public void setFishPerBinPerSex(String fishPerBinPerSex) {
        this.fishPerBinPerSex = fishPerBinPerSex;
    }
}
