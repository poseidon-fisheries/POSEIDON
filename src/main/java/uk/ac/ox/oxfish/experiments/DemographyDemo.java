package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.NaturalMortalityProcess;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentBySpawningBiomass;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Demo to show the population pyramids
 * Created by carrknight on 3/2/16.
 */
public class DemographyDemo {


    public static void main(String[] args) throws IOException {

        //Take DoverSole
        Meristics sole = new Meristics(50, 1, 9.04, 39.91, 0.1713, 0.000002231, 3.412, 0.1417,
                                       1, 5.4, 47.81, 0.1496, 0.000002805, 3.345, 0.1165,
                                       35, -0.775, 1, 0, 404138330, 0.8,
                                       false);
        Species species = new Species("Dover Sole");
        RecruitmentProcess recruitment = new RecruitmentBySpawningBiomass(sole.getVirginRecruits(),
                                                                          sole.getSteepness(),
                                                                          sole.isAddRelativeFecundityToSpawningBiomass());
        NaturalMortalityProcess mortality = new NaturalMortalityProcess();

        int[] femaleData = new int[]{
                148068100,133008100,126543700,112262200,100212000,94965700,86509400,76354500,69498200,64571800,58673073,
                52220801,46478085,41366894,36817781,32768933,29165337,25958027,23103426,20562744,11793506,10496575,
                9342268,8314900,7400511,6586677,5862341,5217660,4643874,4133188,2893657,2575442,2292221,2040146,1815791,
                1616109,1438385,1280206,1139422,1014120,1035151,921316,819999,729824,649565,578132,514555,457970,407607,
                362782,233292};
        int[] maleData = new int[]{
                148068100,129696900,120321200,104084100,90594000,83698600,74321600,63926200,56656100,51168500,43397207,
                37663636,32687576,28368946,24620885,21368013,18544905,16094781,13968364,12122886,5195917,4509440,3913660,
                3396594,2947841,2558377,2220368,1927017,1672422,1451464,611412,530633,460526,399682,346877,301048,261274,
                226755,196797,170796,128829,111809,97037,84216,73090,63433,55053,47779,41467,35988,18364};

        assert maleData.length == femaleData.length;
        assert sole.getMaxAge() == maleData.length;

        //recruitment takes n+2 years so we need some form of queue to contain it
        Queue<Integer> recruits = new LinkedList<>();
        //the first 2 years we keep constaint recruitment
        recruits.add(recruitment.recruit(species,sole,femaleData,maleData));
        recruits.add(recruitment.recruit(species,sole,femaleData,maleData));

        StringBuilder builder = new StringBuilder();
        builder.append("simulation_year,sex,age,number").append("\n");
        for(int simulationYear = 0; simulationYear<50; simulationYear++)
        {



            for(int i=0;i<femaleData.length;i++)
            {
                builder.append(simulationYear).append(",")
                        .append("male,").append(i).
                        append(",").append(maleData[i]).append("\n");
                builder.append(simulationYear).append(",")
                        .append("female,").append(i).
                        append(",").append(femaleData[i]).append("\n");
            }

            //recruit
            recruits.add(recruitment.recruit(species,sole,femaleData,maleData));
            int totalRecruits = recruits.poll();

            //kill
            mortality.cull(maleData,femaleData,sole);

            //age
            System.arraycopy(maleData,0,maleData,1,maleData.length-1);
            System.arraycopy(femaleData,0,femaleData,1,maleData.length-1);
            maleData[0] = totalRecruits/2;
            femaleData[0] = totalRecruits/2;


        }
        System.out.println(recruits.poll() + " recruits ");
        System.out.println(builder.toString());
        Files.write(Paths.get("runs","demography","demography.csv"),builder.toString().getBytes());

    }
}
