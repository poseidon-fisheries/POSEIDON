package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.AbundanceDiffuser;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/8/17.
 */
public class AbundanceDiffusers {


    private AbundanceDiffusers() {
    }


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends AbundanceDiffuser>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();

    static{
        CONSTRUCTORS.put("No Diffusion",
                         NoDiffuserFactory::new);
        NAMES.put(NoDiffuserFactory.class,"No Diffusion");

        CONSTRUCTORS.put("Constant Rate Diffusion",
                         ConstantRateDiffuserFactory::new);
        NAMES.put(ConstantRateDiffuserFactory.class,"Constant Rate Diffusion");

        CONSTRUCTORS.put("Bin-Restricted Diffusion",
                         AgeLimitedConstantRateDiffuserFactory::new);
        NAMES.put(AgeLimitedConstantRateDiffuserFactory.class,"Bin-Restricted Diffusion");
    }


}
