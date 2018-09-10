/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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

        CONSTRUCTORS.put("Random Allocator",
                         RandomAllocatorFactory::new);
        NAMES.put(RandomAllocatorFactory.class,"Random Allocator");

        CONSTRUCTORS.put("Random Smoothed Allocator",
                         RandomSmoothedFactory::new);
        NAMES.put(RandomSmoothedFactory.class,"Random Smoothed Allocator");

        CONSTRUCTORS.put("Random Kernel Allocator",
                         KernelizedRandomFactory::new);
        NAMES.put(KernelizedRandomFactory.class,"Random Kernel Allocator");

        CONSTRUCTORS.put("Simplex Allocator",
                         SimplexFactory::new);
        NAMES.put(SimplexFactory.class,"Simplex Allocator");

        CONSTRUCTORS.put("Pyramids Allocator",
                         PyramidsAllocatorFactory::new);
        NAMES.put(PyramidsAllocatorFactory.class,"Pyramids Allocator");

        CONSTRUCTORS.put("From File Allocator",
                         CoordinateFileAllocatorFactory::new);
        NAMES.put(CoordinateFileAllocatorFactory.class,"From File Allocator");

        CONSTRUCTORS.put("Shape File Allocator",
                PolygonAllocatorFactory::new);
        NAMES.put(PolygonAllocatorFactory.class,"Shape File Allocator");


    }

}
