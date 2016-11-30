package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.BanditDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.MapDiscretization;
import uk.ac.ox.oxfish.geography.SquaresMapDiscretizer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Averager;
import uk.ac.ox.oxfish.model.data.factory.ExponentialMovingAverageFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Locker;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import uk.ac.ox.oxfish.utility.bandit.factory.BanditSupplier;
import uk.ac.ox.oxfish.utility.bandit.factory.EpsilonGreedyBanditFactory;

/**
 * Created by carrknight on 11/10/16.
 */
public class BanditDestinationFactory implements AlgorithmFactory<BanditDestinationStrategy>{


    private int horizontalTicks = 2;

    private int verticalTicks = 2;

    private static Locker<FishState,MapDiscretization> discretizer = new Locker();

    private AlgorithmFactory<? extends Averager> average = new ExponentialMovingAverageFactory();

    private AlgorithmFactory<? extends BanditSupplier> bandit = new EpsilonGreedyBanditFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BanditDestinationStrategy apply(FishState state)
    {

        MapDiscretization map =  discretizer.
                presentKey(state,
                           () -> {
                               MapDiscretization discretization =
                                       new MapDiscretization(
                                               new SquaresMapDiscretizer(
                                                       verticalTicks,
                                                       horizontalTicks));
                               discretization.discretize(state.getMap());
                               return discretization;
                           }
                );


        return new BanditDestinationStrategy(
                arms -> new BanditAverage(
                        arms,
                        average,
                        state
                ),
                bandit.apply(state),
                map,
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

    /**
     * Getter for property 'average'.
     *
     * @return Value for property 'average'.
     */
    public AlgorithmFactory<? extends Averager> getAverage() {
        return average;
    }

    /**
     * Setter for property 'average'.
     *
     * @param average Value to set for property 'average'.
     */
    public void setAverage(AlgorithmFactory<? extends Averager> average) {
        this.average = average;
    }

    /**
     * Getter for property 'bandit'.
     *
     * @return Value for property 'bandit'.
     */
    public AlgorithmFactory<? extends BanditSupplier> getBandit() {
        return bandit;
    }

    /**
     * Setter for property 'bandit'.
     *
     * @param bandit Value to set for property 'bandit'.
     */
    public void setBandit(
            AlgorithmFactory<? extends BanditSupplier> bandit) {
        this.bandit = bandit;
    }
}
