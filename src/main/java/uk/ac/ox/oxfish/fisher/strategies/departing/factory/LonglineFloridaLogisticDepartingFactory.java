package uk.ac.ox.oxfish.fisher.strategies.departing.factory;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.LogisticClassifier;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.ObservationExtractor;
import uk.ac.ox.oxfish.fisher.strategies.departing.DailyLogisticDepartingStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;
import uk.ac.ox.oxfish.utility.Season;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * The logistic decision to go out or not parametrized for Longliners by Steve Saul
 * Created by carrknight on 12/2/16.
 */
public class LonglineFloridaLogisticDepartingFactory implements AlgorithmFactory<DailyLogisticDepartingStrategy>
{


    private DoubleParameter intercept = new FixedDoubleParameter(-3.626);

    private DoubleParameter summer = new FixedDoubleParameter(0.439);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public DailyLogisticDepartingStrategy apply(FishState state) {

        return new DailyLogisticDepartingStrategy(
                new LogisticClassifier(
                        //intercept:
                        new Pair<>(
                                new ObservationExtractor() {
                                    @Override
                                    public double extract(
                                            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
                                        return 1;
                                    }
                                }
                        ,intercept.apply(state.getRandom())),
                        //summer?:
                        new Pair<>(
                                new ObservationExtractor() {
                                    @Override
                                    public double extract(
                                            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
                                        return Season.season(model.getDayOfTheYear()).equals(Season.SUMMER) ? 1 : 0;
                                    }
                                }
                                ,summer.apply(state.getRandom()))
                ));


    }


    /**
     * Getter for property 'intercept'.
     *
     * @return Value for property 'intercept'.
     */
    public DoubleParameter getIntercept() {
        return intercept;
    }

    /**
     * Getter for property 'summer'.
     *
     * @return Value for property 'summer'.
     */
    public DoubleParameter getSummer() {
        return summer;
    }

    public void setIntercept(DoubleParameter intercept) {
        this.intercept = intercept;
    }

    public void setSummer(DoubleParameter summer) {
        this.summer = summer;
    }
}
