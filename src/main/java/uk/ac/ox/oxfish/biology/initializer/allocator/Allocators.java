package uk.ac.ox.oxfish.biology.initializer.allocator;

import uk.ac.ox.oxfish.biology.complicated.factory.FromLeftToRightAllocatorFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Created by carrknight on 7/11/17.
 */
public class Allocators {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends BiomassAllocator>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();



    static
    {


        CONSTRUCTORS.put("Equal Allocation",
                         ConstantAllocatorFactory::new);
        NAMES.put(ConstantAllocatorFactory.class,"Equal Allocation");


        CONSTRUCTORS.put("Bounded Allocation",
                         BoundedAllocatorFactory::new);
        NAMES.put(BoundedAllocatorFactory.class,"Bounded Allocation");

        CONSTRUCTORS.put("From Left to Right Allocation",
                         FromLeftToRightAllocatorFactory::new);
        NAMES.put(FromLeftToRightAllocatorFactory.class,"From Left to Right Allocation");


        CONSTRUCTORS.put("Depth Allocator",
                         DepthAllocatorFactory::new);
        NAMES.put(DepthAllocatorFactory.class,"Depth Allocator");


    }

}
