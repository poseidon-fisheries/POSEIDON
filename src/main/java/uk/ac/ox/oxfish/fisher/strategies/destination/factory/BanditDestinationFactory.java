package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.BanditDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.MapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Averager;
import uk.ac.ox.oxfish.model.data.ExponentialMovingAverage;
import uk.ac.ox.oxfish.model.data.IterativeAverage;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.bandit.BanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.EpsilonGreedyBanditAlgorithm;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by carrknight on 11/10/16.
 */
public class BanditDestinationFactory implements AlgorithmFactory<BanditDestinationStrategy>{


    private int horizontalTicks = 2;

    private int verticalTicks = 2;

    private static MapDiscretizer discretizer;


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BanditDestinationStrategy apply(FishState state)
    {
        if(discretizer == null)
            discretizer = new MapDiscretizer(state.getMap(),verticalTicks,horizontalTicks);


        return new BanditDestinationStrategy(
                (Function<Integer, BanditAverage>) integer -> new BanditAverage(integer,
                                                                                () -> new ExponentialMovingAverage<>(.5)),
                banditAverage -> new EpsilonGreedyBanditAlgorithm(banditAverage,.1),
                discretizer,
                new FavoriteDestinationStrategy(state.getMap(), state.getRandom()));
    }


    /**
     * Getter for property 'horizontalTicks'.
     *
     * @return Value for property 'horizontalTicks'.
     */
    public int getHorizontalTicks() {
        return horizontalTicks;
    }

    /**
     * Setter for property 'horizontalTicks'.
     *
     * @param horizontalTicks Value to set for property 'horizontalTicks'.
     */
    public void setHorizontalTicks(int horizontalTicks) {
        this.horizontalTicks = horizontalTicks;
    }

    /**
     * Getter for property 'verticalTicks'.
     *
     * @return Value for property 'verticalTicks'.
     */
    public int getVerticalTicks() {
        return verticalTicks;
    }

    /**
     * Setter for property 'verticalTicks'.
     *
     * @param verticalTicks Value to set for property 'verticalTicks'.
     */
    public void setVerticalTicks(int verticalTicks) {
        this.verticalTicks = verticalTicks;
    }
}
