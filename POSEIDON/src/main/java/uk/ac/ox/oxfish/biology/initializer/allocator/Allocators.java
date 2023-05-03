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
import uk.ac.ox.oxfish.utility.Constructors;

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
    public static final Map<String, Supplier<AlgorithmFactory<? extends BiomassAllocator>>> CONSTRUCTORS;

    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(ConstantAllocatorFactory.class, "Equal Allocation");
        NAMES.put(BoundedAllocatorFactory.class, "Bounded Allocation");
        NAMES.put(FromLeftToRightAllocatorFactory.class, "From Left to Right Allocation");
        NAMES.put(DepthAllocatorFactory.class, "Depth Allocator");
        NAMES.put(RandomAllocatorFactory.class, "Random Allocator");
        NAMES.put(RandomSmoothedFactory.class, "Random Smoothed Allocator");
        NAMES.put(KernelizedRandomFactory.class, "Random Kernel Allocator");
        NAMES.put(SimplexFactory.class, "Simplex Allocator");
        NAMES.put(PyramidsAllocatorFactory.class, "Pyramids Allocator");
        NAMES.put(SinglePeakAllocatorFactory.class, "Single Peak Pyramid Allocator");
        NAMES.put(MirroredPyramidsAllocatorFactory.class, "Mirrored Peak Pyramid Allocator");
        NAMES.put(CoordinateFileAllocatorFactory.class, "From File Allocator");
        NAMES.put(SmootherFileAllocatorFactory.class, "From File Smoothed Allocator");
        NAMES.put(PolygonAllocatorFactory.class, "Shape File Allocator");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

}
