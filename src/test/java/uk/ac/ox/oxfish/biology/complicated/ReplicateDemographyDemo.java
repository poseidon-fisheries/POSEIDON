package uk.ac.ox.oxfish.biology.complicated;

import com.esotericsoftware.minlog.Log;
import org.junit.BeforeClass;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.experiments.DemographyDemo;
import uk.ac.ox.oxfish.model.FishState;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 3/7/16.
 */
public class ReplicateDemographyDemo {

    public static final Path PATH_TO_FILE = Paths.get("inputs", "tests", "demography.csv");

    @BeforeClass
    public static void setUp() throws Exception {
        if(!Files.exists(PATH_TO_FILE))
            DemographyDemo.main(new String[0]);


    }

    @Test
    public void replicateDemograph() throws Exception {
        Log.info("This demo tries to copy precisely the demo and csvs created by the Demography Demo, which is called at the beginning of the test");
        final long startTime = System.currentTimeMillis();

        //Take DoverSole
        Meristics sole = new Meristics(69,50 , 1, 9.04, 39.91, 0.1713, 0.000002231, 3.412,
                                       0.1417, 1, 5.4, 47.81, 0.1496, 0.000002805, 3.345,
                                       0.1165, 35, -0.775, 1, 0, 404138330,
                                       0.8, false);
        Species species = new Species("Dover Sole", sole);
        RecruitmentProcess recruitment = new RecruitmentBySpawningBiomassDelayed(sole.getVirginRecruits(),
                                                                                 sole.getSteepness(),
                                                                                 sole.isAddRelativeFecundityToSpawningBiomass(),
                                                                                 2);
        NaturalMortalityProcess mortality = new NaturalMortalityProcess();

        int[] femaleData = new int[]{
                148068100,133008100,126543700,112262200,100212000,94965700,86509400,76354500,69498200,64571800,58673073,
                52220801,46478085,41366894,36817781,32768933,29165337,25958027,23103426,20562744,11793506,10496575,
                9342268,8314900,7400511,6586677,5862341,5217660,4643874,4133188,2893657,2575442,2292221,2040146,1815791,
                1616109,1438385,1280206,1139422,1014120,1035151,921316,819999,729824,649565,578132,514555,457970,407607,
                362782,233292,207637,184803,164480,146393,130294,115965,103213,91862,81760,94551,84153,74899,66662,59331,
                52807,46999,41831,37231,33137
        };
        int[] maleData = new int[]{
                148068100,129696900,120321200,104084100,90594000,83698600,74321600,63926200,56656100,51168500,43397207,
                37663636,32687576,28368946,24620885,21368013,18544905,16094781,13968364,12122886,5195917,4509440,3913660,
                3396594,2947841,2558377,2220368,1927017,1672422,1451464,611412,530633,460526,399682,346877,301048,261274,
                226755,196797,170796,128829,111809,97037,84216,73090,63433,55053,47779,41467,35988,18364,
                15938,13832,12005,10419,9042,7848,6811,5911,5130,4377,3799,3297,2862,2483,2155,1871,1623,1409,1223

        };

        assert maleData.length == femaleData.length;
        assert sole.getMaxAge()+1 == maleData.length;

        AbundanceBasedLocalBiology biology = new AbundanceBasedLocalBiology(new GlobalBiology(species));
        for(int age=0; age<femaleData.length; age++)
        {
            biology.getNumberOfFemaleFishPerAge(species)[age] = femaleData[age];
            biology.getNumberOfMaleFishPerAge(species)[age] = maleData[age];
        }

        SingleSpeciesNaturalProcesses processes = new SingleSpeciesNaturalProcesses(mortality, recruitment, species,
                                                                                    false);
        processes.add(biology);

        StringBuilder builder = new StringBuilder();
        builder.append("simulation_year,sex,age,number").append("\n");
        FishState model = mock(FishState.class);
        when(model.getSpecies()).thenReturn(Collections.singletonList(species));
        for(int simulationYear = 0; simulationYear<50; simulationYear++)
        {


            int[] male = biology.getNumberOfMaleFishPerAge(species);
            int[] female = biology.getNumberOfFemaleFishPerAge(species);

            for(int i=0;i<femaleData.length;i++)
            {
                builder.append(simulationYear).append(",")
                        .append("male,").append(i).
                        append(",").append(male[i]).append("\n");
                builder.append(simulationYear).append(",")
                        .append("female,").append(i).
                        append(",").append(female[i]).append("\n");
            }

            processes.step(model);


        }
        System.out.println(processes.getLastRecruits() + " recruits ");
        System.out.println(builder.toString());

        String original = String.join("\n", Files.readAllLines(PATH_TO_FILE)) + "\n";

        assertEquals(original,builder.toString());

        final long endTime = System.currentTimeMillis();

        System.out.println("Total execution time: " + (endTime - startTime) );

    }

    @Test
    public void replicateDemographSplitInTwo() throws Exception {
        Log.info("This demo tries to copy precisely the demo and csvs created by the Demography Demo but only after splitting" +
                         "the world into 2 separate tiles (while the original demo has no geography)");
        final long startTime = System.currentTimeMillis();

        //Take DoverSole
        Meristics sole = new Meristics(69,50 , 1, 9.04, 39.91, 0.1713, 0.000002231, 3.412,
                                       0.1417, 1, 5.4, 47.81, 0.1496, 0.000002805, 3.345,
                                       0.1165, 35, -0.775, 1, 0, 404138330,
                                       0.8, false);
        Species species = new Species("Dover Sole", sole);
        RecruitmentProcess recruitment = new RecruitmentBySpawningBiomassDelayed(sole.getVirginRecruits(),
                                                                                 sole.getSteepness(),
                                                                                 sole.isAddRelativeFecundityToSpawningBiomass(),
                                                                                 2);
        NaturalMortalityProcess mortality = new NaturalMortalityProcess();

        int[] femaleData = new int[]{
                148068100,133008100,126543700,112262200,100212000,94965700,86509400,76354500,69498200,64571800,58673073,
                52220801,46478085,41366894,36817781,32768933,29165337,25958027,23103426,20562744,11793506,10496575,
                9342268,8314900,7400511,6586677,5862341,5217660,4643874,4133188,2893657,2575442,2292221,2040146,1815791,
                1616109,1438385,1280206,1139422,1014120,1035151,921316,819999,729824,649565,578132,514555,457970,407607,
                362782,233292,207637,184803,164480,146393,130294,115965,103213,91862,81760,94551,84153,74899,66662,59331,
                52807,46999,41831,37231,33137
        };
        int[] maleData = new int[]{
                148068100,129696900,120321200,104084100,90594000,83698600,74321600,63926200,56656100,51168500,43397207,
                37663636,32687576,28368946,24620885,21368013,18544905,16094781,13968364,12122886,5195917,4509440,3913660,
                3396594,2947841,2558377,2220368,1927017,1672422,1451464,611412,530633,460526,399682,346877,301048,261274,
                226755,196797,170796,128829,111809,97037,84216,73090,63433,55053,47779,41467,35988,18364,
                15938,13832,12005,10419,9042,7848,6811,5911,5130,4377,3799,3297,2862,2483,2155,1871,1623,1409,1223

        };

        assert maleData.length == femaleData.length;
        assert sole.getMaxAge()+1 == maleData.length;

        AbundanceBasedLocalBiology biology1 = new AbundanceBasedLocalBiology(new GlobalBiology(species));
        AbundanceBasedLocalBiology biology2 = new AbundanceBasedLocalBiology(new GlobalBiology(species));
        for(int age=0; age<femaleData.length; age++)
        {
            biology1.getNumberOfFemaleFishPerAge(species)[age] = femaleData[age]/3;
            biology2.getNumberOfFemaleFishPerAge(species)[age] = femaleData[age] -
                    biology1.getNumberOfFemaleFishPerAge(species)[age];

            biology1.getNumberOfMaleFishPerAge(species)[age] = maleData[age]/3;
            biology2.getNumberOfMaleFishPerAge(species)[age] = maleData[age] -
                    biology1.getNumberOfMaleFishPerAge(species)[age];
        }

        SingleSpeciesNaturalProcesses processes = new SingleSpeciesNaturalProcesses(mortality, recruitment, species,
                                                                                    false);
        processes.add(biology1);
        processes.add(biology2);

        StringBuilder builder = new StringBuilder();
        builder.append("simulation_year,sex,age,number").append("\n");
        FishState model = mock(FishState.class);
        when(model.getSpecies()).thenReturn(Collections.singletonList(species));
        for(int simulationYear = 0; simulationYear<50; simulationYear++)
        {


            for(int i=0;i<femaleData.length;i++)
            {
                builder.append(simulationYear).append(",")
                        .append("male,").append(i).
                        append(",").append(
                        biology1.getNumberOfMaleFishPerAge(species)[i] +
                                biology2.getNumberOfMaleFishPerAge(species)[i]
                ).append("\n");
                builder.append(simulationYear).append(",")
                        .append("female,").append(i).
                        append(",").append(
                        biology1.getNumberOfFemaleFishPerAge(species)[i] +
                                biology2.getNumberOfFemaleFishPerAge(species)[i]
                ).append("\n");
            }

            processes.step(model);


        }
        System.out.println(processes.getLastRecruits() + " recruits ");
        System.out.println(builder.toString());

        List<String> originalData = Files.readAllLines(PATH_TO_FILE);

        //unfortunately when we split the data mortality gets a bit screwy with roundings so that we can't guarantee
        //that the numbers will be precisely the same, but we can bound the error to be very small!
        String[] newData = builder.toString().split("\n");

        Iterator<String> iterator = originalData.iterator();
        iterator.next();
        int i=1;
        while(iterator.hasNext())
        {
            double original = Integer.parseInt(iterator.next().split(",")[3]);
            double current = Integer.parseInt(newData[i].split(",")[3]);
            i++;
            System.out.println(original + " ----- " + current);
            assertEquals(original,current,5); //never more than 5 fish difference
        }



    }
}