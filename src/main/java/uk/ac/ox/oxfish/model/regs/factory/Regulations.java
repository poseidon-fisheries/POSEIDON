package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Just a map with a link to all the constructors
 * Created by carrknight on 6/14/15.
 */
public class Regulations
{


    /**
     * I am forcing the TAC factory itself to be a singleton so that i force all the TACs to remain connected
     */
    private static final TACMonoFactory TAC_MONO_FACTORY = new TACMonoFactory();

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends Regulation>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();

    static
    {
        CONSTRUCTORS.put("Anarchy", AnarchyFactory::new);
        NAMES.put(AnarchyFactory.class,"Anarchy");

        CONSTRUCTORS.put("Fishing Season", FishingSeasonFactory::new);
        NAMES.put(FishingSeasonFactory.class,"Fishing Season");

        CONSTRUCTORS.put("MPA Only", ProtectedAreasOnlyFactory::new);
        NAMES.put(ProtectedAreasOnlyFactory.class,"MPA Only");

        CONSTRUCTORS.put("Mono-TAC", () -> TAC_MONO_FACTORY);
        NAMES.put(TACMonoFactory.class,"Mono-TAC");

        CONSTRUCTORS.put("Mono-IQ", IQMonoFactory::new);
        NAMES.put(IQMonoFactory.class,"Mono-IQ");

        CONSTRUCTORS.put("Mono-ITQ", ITQMonoFactory::new);
        NAMES.put(ITQMonoFactory.class,"Mono-ITQ");

        CONSTRUCTORS.put("Multi-ITQ", MultiITQFactory::new);
        NAMES.put(MultiITQFactory.class, "Multi-ITQ");

        CONSTRUCTORS.put("Multi-ITQ by List", MultiITQStringFactory::new);
        NAMES.put(MultiITQStringFactory.class, "Multi-ITQ by List");

        CONSTRUCTORS.put("Partial-ITQ", ITQSpecificFactory::new);
        NAMES.put(ITQSpecificFactory.class,"Partial-ITQ");

        CONSTRUCTORS.put("Multi-TAC", TACMultiFactory::new);
        NAMES.put(TACMultiFactory.class,"Multi-TAC");

        CONSTRUCTORS.put("Multi-TAC by List", MultiTACStringFactory::new);
        NAMES.put(MultiTACStringFactory.class,"Multi-TAC");

        CONSTRUCTORS.put("Kitchen Sink", KitchenSinkFactory::new);
        NAMES.put(KitchenSinkFactory.class,"Kitchen Sink");



    }




}
