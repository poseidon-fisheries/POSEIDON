package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.google.common.base.Preconditions;
import org.jenetics.util.Factory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;

import java.util.function.Function;

/**
 * A simple strategy for departure where the fisher decides to get out of port at random with fixed probability
 * Created by carrknight on 4/18/15.
 */
public class FixedProbabilityDepartingStrategy implements DepartingStrategy {

    private final double probabilityToLeavePort;


    public FixedProbabilityDepartingStrategy(double probabilityToLeavePort)
    {
        Preconditions.checkArgument(probabilityToLeavePort >= 0, "Probability can't be negative!");
        Preconditions.checkArgument(probabilityToLeavePort <= 1, "Probability can't be above 1");
        this.probabilityToLeavePort = probabilityToLeavePort;
    }


    /**
     * ignored
     */
    @Override
    public void start(FishState model) {
        //nothing happens
    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @param fisher the fisher who is deciding whether to move or not
     * @param model the model. Not used
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(Fisher fisher, FishState model) {
        return fisher.getRandom().nextBoolean(probabilityToLeavePort);
    }


    public double getProbabilityToLeavePort() {
        return probabilityToLeavePort;
    }


    /**
     * the factory to create as many
     */
    public static StrategyFactory<FixedProbabilityDepartingStrategy> factory =
             new FixedProbabilityDepartingFactory();
}

class FixedProbabilityDepartingFactory implements StrategyFactory<FixedProbabilityDepartingStrategy>
{
    private double probabilityToLeavePort=0;

    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedProbabilityDepartingStrategy apply(FishState state) {
        return new FixedProbabilityDepartingStrategy(probabilityToLeavePort);
    }


    public double getProbabilityToLeavePort() {
        return probabilityToLeavePort;
    }

    public void setProbabilityToLeavePort(double probabilityToLeavePort) {
        Preconditions.checkArgument(probabilityToLeavePort >= 0, "Probability can't be negative!");
        Preconditions.checkArgument(probabilityToLeavePort <= 1, "Probability can't be above 1");
        this.probabilityToLeavePort = probabilityToLeavePort;
    }

    /**
     * sometimes I have a factory but I want to know what is the superclass it generates. It would be nice to do this through reflection
     * but that's actually not possible: http://www.angelikalanger.com/GenericsFAQ/FAQSections/ProgrammingIdioms.html#Which information related to generics can I access reflectively?
     * so I do it through a method. Somewhere I have to keep a big map linking strategies super-classes with constructor lists
     *
     * @return the strategy super-class
     */
    @Override
    public Class<? super FixedProbabilityDepartingStrategy> getStrategySuperClass() {
        return DepartingStrategy.class;
    }
}
