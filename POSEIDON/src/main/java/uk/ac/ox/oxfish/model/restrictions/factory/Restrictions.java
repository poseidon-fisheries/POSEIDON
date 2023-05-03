package uk.ac.ox.oxfish.model.restrictions.factory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import uk.ac.ox.oxfish.model.restrictions.Restriction;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * 
 * A list of possible communal restrictions types.
 * 
 * 
 * @author Brian Powers 5/17/2019
 *
 */

public class Restrictions {

    public static final Map<String,Supplier<AlgorithmFactory<? extends Restriction>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();
    
    static
    {
        CONSTRUCTORS.put("One Religious Holiday", OneReligiousHolidayFactory::new);
        NAMES.put(OneReligiousHolidayFactory.class,"One Religious Holiday");

    }
}
