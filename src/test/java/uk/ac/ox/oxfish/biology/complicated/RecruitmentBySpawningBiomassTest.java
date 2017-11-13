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

package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;

import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by carrknight on 6/26/17.
 */
public class RecruitmentBySpawningBiomassTest {

    @Test
    public void recruitment() throws Exception {

        Species species = MultipleSpeciesAbundanceInitializer.
                generateSpeciesFromFolder(Paths.get("inputs",
                                                    "california",
                                                    "biology",
                                                    "Sablefish"), "Sablefish");

        double[] male = new double[60];
        double[] female = new double[60];
        Arrays.fill(male, 0);
        Arrays.fill(female, 10000);

        System.out.println(species.getVirginRecruits());
        System.out.println(species.getSteepness());
        System.out.println(species.isAddRelativeFecundityToSpawningBiomass());

        RecruitmentBySpawningBiomass process = new RecruitmentBySpawningBiomass(
                species.getVirginRecruits(),
                species.getSteepness(),
                species.getCumulativePhi(),
                species.isAddRelativeFecundityToSpawningBiomass()
        );


        double recruits = process.recruit(species,species.getMeristics(),female,male);
        System.out.println(recruits);
        Assert.assertEquals(416140d, recruits, 1d);

    }
}