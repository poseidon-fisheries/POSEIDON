package uk.ac.ox.oxfish.biology.complicated;

import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.complicated.factory.MeristicsFileFactory;
import uk.ac.ox.oxfish.biology.initializer.MultipleSpeciesAbundanceInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SpreadYearlyRecruitDecoratorTest {


    /**
     * recycling RecruitmentBySpawningBiomassTest but now splitting it into 2 separate recruitment pulses
     * @throws Exception
     */
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
        double recruits = recruitment.recruit(process.getSpecies(),meristics,
                new StructuredAbundance(male,female),0 ,365 );
        Assert.assertEquals(416140d, recruits, 1d);


        //DECORATION
        LinkedHashMap<Integer, DoubleParameter> map = new LinkedHashMap<>();
        map.put(100,new FixedDoubleParameter(0.3));
        map.put(200,new FixedDoubleParameter(0.7));
        SpreadYearlyRecruitDecorator decorator = new SpreadYearlyRecruitDecorator(map,
                recruitment,new MersenneTwisterFast());

        Assert.assertEquals(0d,
                decorator.recruit(process.getSpecies(),meristics,
                        new StructuredAbundance(male,female),0 ,1 )

                , 1d);

        Assert.assertEquals(0d,
                decorator.recruit(process.getSpecies(),meristics,
                        new StructuredAbundance(male,female),364 ,1 )

                , 1d);

        Assert.assertEquals(416140d*0.3d,
                decorator.recruit(process.getSpecies(),meristics,
                        new StructuredAbundance(male,female),100 ,1 )

                , 1d);

        Assert.assertEquals(416140d*0.7d,
                decorator.recruit(process.getSpecies(),meristics,
                        new StructuredAbundance(male,female),200 ,1 )

                , 1d);

    }


}