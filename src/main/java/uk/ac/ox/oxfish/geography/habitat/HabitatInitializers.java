package uk.ac.ox.oxfish.geography.habitat;

import uk.ac.ox.oxfish.geography.habitat.rectangles.OneRockyRectangleFactory;
import uk.ac.ox.oxfish.geography.habitat.rectangles.RockyRectanglesHabitatFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Big ol' list of all the habitat initializers' factories
 * Created by carrknight on 9/29/15.
 */
public class HabitatInitializers {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory
            <? extends HabitatInitializer>>> CONSTRUCTORS =
            new LinkedHashMap<>();

    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();

    static {


        CONSTRUCTORS.put("All Sand",
                         AllSandyHabitatFactory::new);
        NAMES.put(AllSandyHabitatFactory.class,"All Sand");
        CONSTRUCTORS.put("Rocky Rectangles",
                         RockyRectanglesHabitatFactory::new);
        NAMES.put(RockyRectanglesHabitatFactory.class,"Rocky Rectangles");

        CONSTRUCTORS.put("One Rocky Rectangle",
                         OneRockyRectangleFactory::new);
        NAMES.put(OneRockyRectangleFactory.class,"One Rocky Rectangle");

        CONSTRUCTORS.put("Rocky Pyramids",
                         RockyPyramidsFactory::new);
        NAMES.put(RockyPyramidsFactory.class,"Rocky Pyramids");
    }

    private HabitatInitializers() {
    }
}
