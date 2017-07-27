package uk.ac.ox.oxfish.fisher.strategies.fishing.factory;

import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.QuotaLimitDecorator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 7/27/17.
 */
public class QuotaLimitDecoratorFactory implements AlgorithmFactory<QuotaLimitDecorator>{

    private AlgorithmFactory<? extends FishingStrategy> decorated =
            new TowLimitFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public QuotaLimitDecorator apply(FishState state) {
        return new QuotaLimitDecorator(decorated.apply(state));
    }

    /**
     * Getter for property 'decorated'.
     *
     * @return Value for property 'decorated'.
     */
    public AlgorithmFactory<? extends FishingStrategy> getDecorated() {
        return decorated;
    }

    /**
     * Setter for property 'decorated'.
     *
     * @param decorated Value to set for property 'decorated'.
     */
    public void setDecorated(
            AlgorithmFactory<? extends FishingStrategy> decorated) {
        this.decorated = decorated;
    }
}
