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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A list of all constructors for biology initializers
 * Created by carrknight on 6/22/15.
 *
 */
public class BiologyInitializers {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends BiologyInitializer>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();
    static{
        CONSTRUCTORS.put("Independent Logistic",
                         IndependentLogisticFactory::new);
        NAMES.put(IndependentLogisticFactory.class,"Independent Logistic");

        CONSTRUCTORS.put("Diffusing Logistic",
                         DiffusingLogisticFactory::new);
        NAMES.put(DiffusingLogisticFactory.class,"Diffusing Logistic");


        CONSTRUCTORS.put("Habitat-Aware Diffusing Logistic",
                         RockyLogisticFactory::new);
        NAMES.put(RockyLogisticFactory.class,"Habitat-Aware Diffusing Logistic");

        CONSTRUCTORS.put("Habitat-Aware 2 Species",
                         TwoSpeciesRockyLogisticFactory::new);
        NAMES.put(TwoSpeciesRockyLogisticFactory.class,"Habitat-Aware 2 Species");


        CONSTRUCTORS.put("From Left To Right Fixed",
                         FromLeftToRightFactory::new);
        NAMES.put(FromLeftToRightFactory.class,"From Left To Right Fixed");

        CONSTRUCTORS.put("From Left To Right Logistic",
                         FromLeftToRightLogisticFactory::new);
        NAMES.put(FromLeftToRightLogisticFactory.class,"From Left To Right Logistic");

        CONSTRUCTORS.put("From Left To Right Logistic with Climate Change",
                         FromLeftToRightLogisticPlusClimateChangeFactory::new);
        NAMES.put(FromLeftToRightLogisticPlusClimateChangeFactory.class,"From Left To Right Logistic with Climate Change");

        CONSTRUCTORS.put("From Left To Right Well-Mixed",
                         FromLeftToRightMixedFactory::new);
        NAMES.put(FromLeftToRightMixedFactory.class,"From Left To Right Well-Mixed");

        CONSTRUCTORS.put("Random Smoothed and Fixed",
                         RandomConstantBiologyFactory::new);
        NAMES.put(RandomConstantBiologyFactory.class,"Random Smoothed and Fixed");

        CONSTRUCTORS.put("Half Bycatch",
                         HalfBycatchFactory::new);
        NAMES.put(HalfBycatchFactory.class,"Half Bycatch");

        CONSTRUCTORS.put("Split in Half",
                         SplitInitializerFactory::new);
        NAMES.put(SplitInitializerFactory.class,"Split in Half");

        CONSTRUCTORS.put("Well-Mixed",
                         WellMixedBiologyFactory::new);
        NAMES.put(WellMixedBiologyFactory.class,"Well-Mixed");

        CONSTRUCTORS.put("Osmose Biology",
                         OsmoseBiologyFactory::new);
        NAMES.put(OsmoseBiologyFactory.class,"Osmose Biology");

        CONSTRUCTORS.put("Two Species Box",
                         TwoSpeciesBoxFactory::new);
        NAMES.put(TwoSpeciesBoxFactory.class,"Two Species Box");


        CONSTRUCTORS.put("Single Species Biomass",
                         SingleSpeciesBiomassFactory::new);
        NAMES.put(SingleSpeciesBiomassFactory.class, "Single Species Biomass");

        CONSTRUCTORS.put("Single Species Biomass Normalized",
                         SingleSpeciesBiomassNormalizedFactory::new);
        NAMES.put(SingleSpeciesBiomassNormalizedFactory.class, "Single Species Biomass Normalized");

        CONSTRUCTORS.put("Single Species Abundance From Directory",
                         SingleSpeciesAbundanceFromDirectoryFactory::new);
        NAMES.put(SingleSpeciesAbundanceFromDirectoryFactory.class, "Single Species Abundance From Directory");

        CONSTRUCTORS.put("Single Species Abundance",
                         SingleSpeciesAbundanceFactory::new);
        NAMES.put(SingleSpeciesAbundanceFactory.class, "Single Species Abundance");


        CONSTRUCTORS.put("Multiple Species Biomass",
                         MultipleIndependentSpeciesBiomassFactory::new);
        NAMES.put(MultipleIndependentSpeciesBiomassFactory.class, "Multiple Species Biomass");


        CONSTRUCTORS.put("Multiple Species Abundance",
                MultipleIndependentSpeciesAbundanceFactory::new);
        NAMES.put(MultipleIndependentSpeciesAbundanceFactory.class, "Multiple Species Abundance");

        CONSTRUCTORS.put("One Species School",
                         OneSpeciesSchoolFactory::new);
        NAMES.put(OneSpeciesSchoolFactory.class,"One Species School");


        CONSTRUCTORS.put("Yellow Bycatch Factory",
                         YellowBycatchFactory::new);
        NAMES.put(YellowBycatchFactory.class,"Yellow Bycatch Factory");


        CONSTRUCTORS.put("Yellow Bycatch Factory with History",
                         YellowBycatchWithHistoryFactory::new);
        NAMES.put(YellowBycatchWithHistoryFactory.class,"Yellow Bycatch Factory with History");


        CONSTRUCTORS.put("Linear Getter Biology",
                         LinearGetterBiologyFactory::new);
        NAMES.put(LinearGetterBiologyFactory.class,"Linear Getter Biology");



        CONSTRUCTORS.put("Boxcar Biology",
                         SingleSpeciesBoxcarFactory::new);
        NAMES.put(SingleSpeciesBoxcarFactory.class,"Boxcar Biology");


    }

    private BiologyInitializers() {}


}
