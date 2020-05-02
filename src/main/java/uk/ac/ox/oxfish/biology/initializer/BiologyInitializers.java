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

package uk.ac.ox.oxfish.biology.initializer;

import uk.ac.ox.oxfish.biology.initializer.factory.*;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A list of all constructors for biology initializers
 * Created by carrknight on 6/22/15.
 */
public class BiologyInitializers {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String, Supplier<AlgorithmFactory<? extends BiologyInitializer>>> CONSTRUCTORS;

    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(IndependentLogisticFactory.class, "Independent Logistic");
        NAMES.put(DiffusingLogisticFactory.class, "Diffusing Logistic");
        NAMES.put(RockyLogisticFactory.class, "Habitat-Aware Diffusing Logistic");
        NAMES.put(TwoSpeciesRockyLogisticFactory.class, "Habitat-Aware 2 Species");
        NAMES.put(FromLeftToRightFactory.class, "From Left To Right Fixed");
        NAMES.put(FromLeftToRightLogisticFactory.class, "From Left To Right Logistic");
        NAMES.put(FromLeftToRightLogisticPlusClimateChangeFactory.class, "From Left To Right Logistic with Climate Change");
        NAMES.put(FromLeftToRightMixedFactory.class, "From Left To Right Well-Mixed");
        NAMES.put(RandomConstantBiologyFactory.class, "Random Smoothed and Fixed");
        NAMES.put(HalfBycatchFactory.class, "Half Bycatch");
        NAMES.put(SplitInitializerFactory.class, "Split in Half");
        NAMES.put(WellMixedBiologyFactory.class, "Well-Mixed");
        NAMES.put(OsmoseBiologyFactory.class, "Osmose Biology");
        NAMES.put(TwoSpeciesBoxFactory.class, "Two Species Box");
        NAMES.put(SingleSpeciesBiomassFactory.class, "Single Species Biomass");
        NAMES.put(SingleSpeciesBiomassNormalizedFactory.class, "Single Species Biomass Normalized");
        NAMES.put(SingleSpeciesAbundanceFromDirectoryFactory.class, "Single Species Abundance From Directory");
        NAMES.put(SingleSpeciesAbundanceFactory.class, "Single Species Abundance");
        NAMES.put(MultipleIndependentSpeciesBiomassFactory.class, "Multiple Species Biomass");
        NAMES.put(MultipleIndependentSpeciesAbundanceFactory.class, "Multiple Species Abundance");
        NAMES.put(OneSpeciesSchoolFactory.class, "One Species School");
        NAMES.put(YellowBycatchFactory.class, "Yellow Bycatch Factory");
        NAMES.put(YellowBycatchWithHistoryFactory.class, "Yellow Bycatch Factory with History");
        NAMES.put(LinearGetterBiologyFactory.class, "Linear Getter Biology");
        NAMES.put(SingleSpeciesBoxcarFactory.class, "Boxcar Biology");
        NAMES.put(SingleSpeciesBoxcarPulseRecruitmentFactory.class, "Boxcar Biology with pulses");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private BiologyInitializers() {}
}
