package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.AgingProcess;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/11/17.
 */
public class Recruitments {




    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends RecruitmentProcess>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();



    static
    {

        CONSTRUCTORS.put("Logistic Recruitment",
                         LogisticRecruitmentFactory::new);
        NAMES.put(LogisticRecruitmentFactory.class,"Logistic Recruitment");

        CONSTRUCTORS.put("Beverton-Holt",
                         RecruitmentBySpawningFactory::new);
        NAMES.put(RecruitmentBySpawningFactory.class,"Beverton-Holt");

    }


}
