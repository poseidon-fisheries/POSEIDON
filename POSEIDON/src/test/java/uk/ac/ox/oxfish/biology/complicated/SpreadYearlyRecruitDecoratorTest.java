/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.complicated;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.complicated.factory.MeristicsFileFactory;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.mockito.Mockito.mock;

public class SpreadYearlyRecruitDecoratorTest {


    /**
     * recycling RecruitmentBySpawningBiomassTest but now splitting it into 2 separate recruitment pulses
     *
     * @throws Exception
     */
    @Test
    public void recruitment() throws Exception {


        MeristicsFileFactory factory = new MeristicsFileFactory(Paths.get("inputs",
            "california",
            "biology",
            "Sablefish", "meristics.yaml"
        ));

        StockAssessmentCaliforniaMeristics meristics = factory.apply(mock(FishState.class));
        SingleSpeciesNaturalProcesses process = MultipleSpeciesAbundanceInitializer.initializeNaturalProcesses(
            mock(FishState.class),
            MultipleSpeciesAbundanceInitializer.
                generateSpeciesFromFolder(Paths.get(
                    "inputs",
                    "california",
                    "biology",
                    "Sablefish"
                ), "Sablefish"),
            new HashMap<>(),
            meristics,
            true,
            0,
            false

        );
        double[] male = new double[60];
        double[] female = new double[60];
        Arrays.fill(male, 0);
        Arrays.fill(female, 10000);

        RecruitmentBySpawningBiomass recruitment = (RecruitmentBySpawningBiomass) process.getRecruitment();
        double recruits = recruitment.recruit(process.getSpecies(), meristics,
            new StructuredAbundance(male, female), 0, 365
        );
        Assertions.assertEquals(416140d, recruits, 1d);


        //DECORATION
        LinkedHashMap<Integer, DoubleParameter> map = new LinkedHashMap<>();
        map.put(100, new FixedDoubleParameter(0.3));
        map.put(200, new FixedDoubleParameter(0.7));
        SpreadYearlyRecruitDecorator decorator = new SpreadYearlyRecruitDecorator(map,
            recruitment, new MersenneTwisterFast()
        );

        Assertions.assertEquals(0d, decorator.recruit(process.getSpecies(), meristics,
            new StructuredAbundance(male, female), 0, 1
        ), 1d);

        Assertions.assertEquals(0d, decorator.recruit(process.getSpecies(), meristics,
            new StructuredAbundance(male, female), 364, 1
        ), 1d);

        Assertions.assertEquals(416140d * 0.3d, decorator.recruit(process.getSpecies(), meristics,
            new StructuredAbundance(male, female), 100, 1
        ), 1d);

        Assertions.assertEquals(416140d * 0.7d, decorator.recruit(process.getSpecies(), meristics,
            new StructuredAbundance(male, female), 200, 1
        ), 1d);

    }


}
