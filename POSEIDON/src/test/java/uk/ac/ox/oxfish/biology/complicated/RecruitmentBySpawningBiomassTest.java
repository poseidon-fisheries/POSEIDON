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

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.boxcars.EquallySpacedBertalanffyFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.MeristicsFileFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.RecruitmentBySpawningJackKnifeMaturity;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 6/26/17.
 */
public class RecruitmentBySpawningBiomassTest {

    @Test
    public void recruitment() throws Exception {

        MeristicsFileFactory factory = new MeristicsFileFactory(Paths.get("inputs",
                                                                          "california",
                                                                          "biology",
                                                                          "Sablefish","meristics.yaml"));

        StockAssessmentCaliforniaMeristics meristics = factory.apply(mock(FishState.class));
        SingleSpeciesNaturalProcesses process = MultipleSpeciesAbundanceInitializer.initializeNaturalProcesses(
                mock(FishState.class),
                MultipleSpeciesAbundanceInitializer.
                        generateSpeciesFromFolder(Paths.get("inputs",
                                                            "california",
                                                            "biology",
                                                            "Sablefish"), "Sablefish"),
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
        System.out.println(recruitment.getVirginRecruits());
        System.out.println(recruitment.getSteepness());
        System.out.println(recruitment.isAddRelativeFecundityToSpawningBiomass());



        double recruits = recruitment.recruit(process.getSpecies(),meristics,
                                              new StructuredAbundance(male,female),0 ,365 );
        System.out.println(recruits);
        Assert.assertEquals(416140d, recruits, 1d);

    }


    @Test
    public void lengthAtMaturity() {
        FishState state = mock(FishState.class);
        when(state.getRandom()).thenReturn(new MersenneTwisterFast());



        EquallySpacedBertalanffyFactory factory = new EquallySpacedBertalanffyFactory();
        factory.setAllometricAlpha(new FixedDoubleParameter(0.02));
        factory.setAllometricBeta(new FixedDoubleParameter(2.944));
        factory.setkYearlyParameter(new FixedDoubleParameter(0.4552368));
        factory.setMaxLengthInCm(new FixedDoubleParameter(86));
        factory.setRecruitLengthInCm(new FixedDoubleParameter(0));
        factory.setNumberOfBins(25);
        factory.setCmPerBin(5.0);
        GrowthBinByList meristics = factory.apply(state);
        for(int i=0; i<25; i++)
            Assert.assertEquals(2.5+i*5d,meristics.getLength(0,i),.001);
        //create 25 bins, each of 5 cm


        RecruitmentBySpawningJackKnifeMaturity recruitment = new RecruitmentBySpawningJackKnifeMaturity();
        recruitment.setLengthAtMaturity(50);
        RecruitmentBySpawningBiomass recruit = recruitment.apply(state);
        double[] maturities = recruit.getMaturity().apply(new Species("test", meristics));

        for(int i=0; i<=9; i++)
            Assert.assertEquals(maturities[i],0,.001);

        for(int i=10; i<25; i++)
            Assert.assertEquals(maturities[i],1,.001);



    }
}