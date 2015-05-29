package uk.ac.ox.oxfish.utility;

import uk.ac.ox.oxfish.model.FishState;

import java.util.function.Function;

/**
 * It will be useful to have strategies created by a factory so that I can have the JSON and GUI instantiation separated
 * from the strategy itself. This is the main interface holding everything together
 *
 * Created by carrknight on 5/27/15.
 */
public interface StrategyFactory<T> extends Function<FishState,T>
{


    /**
     * sometimes I have a factory but I want to know what is the superclass it generates. It would be nice to do this through reflection
     * but that's actually not possible: http://www.angelikalanger.com/GenericsFAQ/FAQSections/ProgrammingIdioms.html#Which information related to generics can I access reflectively?
     * so I do it through a method. Somewhere I have to keep a big map linking strategies super-classes with constructor lists
     * @return the strategy super-class
     */
    public Class<? super T> getStrategySuperClass();

}
