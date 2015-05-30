package uk.ac.ox.oxfish.fisher.strategies.departing;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.StrategyFactory;

public class FixedProbabilityDepartingFactory implements StrategyFactory<FixedProbabilityDepartingStrategy>
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
        if(probabilityToLeavePort < 0 || probabilityToLeavePort > 1)
            System.err.println("Probability has to be in [0,1]. New value is ignored");
        else
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
