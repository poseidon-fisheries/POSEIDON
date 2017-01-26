package uk.ac.ox.oxfish.fisher.strategies.destination.factory;

import uk.ac.ox.oxfish.fisher.strategies.destination.BanditDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretizer;
import uk.ac.ox.oxfish.geography.discretization.SquaresMapDiscretizerFactory;
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



    private static Locker<FishState,MapDiscretization> locker = new Locker();

    private AlgorithmFactory<? extends Averager> average = new ExponentialMovingAverageFactory();

    private AlgorithmFactory<? extends BanditSupplier> bandit = new EpsilonGreedyBanditFactory();


    private AlgorithmFactory<? extends MapDiscretizer> discretizer = new SquaresMapDiscretizerFactory();



    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BanditDestinationStrategy apply(FishState state)
    {

        MapDiscretization map =  locker.
                presentKey(state,
                           () -> {
                               MapDiscretization discretization =
                                       new MapDiscretization(
                                               discretizer.apply(state));
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

    /**
     * Getter for property 'discretizer'.
     *
     * @return Value for property 'discretizer'.
     */
    public AlgorithmFactory<? extends MapDiscretizer> getDiscretizer() {
        return discretizer;
    }

    /**
     * Setter for property 'discretizer'.
     *
     * @param discretizer Value to set for property 'discretizer'.
     */
    public void setDiscretizer(
            AlgorithmFactory<? extends MapDiscretizer> discretizer) {
        this.discretizer = discretizer;
    }
}
