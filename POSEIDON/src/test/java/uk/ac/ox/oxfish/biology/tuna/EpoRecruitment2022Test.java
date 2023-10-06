package uk.ac.ox.oxfish.biology.tuna;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoAbundanceScenario;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EpoRecruitment2022Test {

    @Test
    public void testRunBiologyOnlyScenario() {
        final EpoAbundanceScenario scenario = new EpoAbundanceScenario();
/*        ((AbundanceMortalityProcessFromFileFactory)
            ((ScheduledAbundanceProcessesFactory) scenario.getBiologicalProcesses()
                .getScheduledProcesses())
                .getAbundanceMortalityProcess())
            .setMortalityFile(scenario.testFolder().path("mortality_BP.csv"));*/
        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        scenario.getFadMap().setCurrentPatternMapSupplier(CurrentPatternMapSupplier.EMPTY);

        fishState.start();

        System.out.println("SeaTiles: " + fishState.getMap().getAllSeaTiles().size());
        final List<Species> specieses = fishState.getSpecies();

        final Species BET = fishState.getSpecies("Bigeye tuna");
        final Species SKJ = fishState.getSpecies("Skipjack tuna");
        final Species YFT = fishState.getSpecies("Yellowfin tuna");
        final double[][] initialAbundanceBET = fishState.getTotalAbundance(BET);
        final double[][] initialAbundanceSKJ = fishState.getTotalAbundance(SKJ);
        final double[][] initialAbundanceYFT = fishState.getTotalAbundance(YFT);


//        System.out.println("breakpoint");


        final int[] checkDays = {90, 181, 273};

        do {

            if (ArrayUtils.contains(checkDays, fishState.getStep())) {
//                System.out.println(fishState.getStep() + " " + fishState.getTotalBiomass(fishState.getSpecies(
 //                   "Bigeye tuna")) / 1000);
                final double[][] totalAbundanceBET = fishState.getTotalAbundance(BET);
                final double[][] totalAbundanceSKJ = fishState.getTotalAbundance(SKJ);
                final double[][] totalAbundanceYFT = fishState.getTotalAbundance(YFT);
//                System.out.println(fishState.getStep() + "BET[0]= " + (totalAbundanceBET[0][0]+totalAbundanceBET[1][0]));
 //               System.out.println(fishState.getStep() + "SKJ[0]= " + (totalAbundanceSKJ[0][0]+totalAbundanceSKJ[1][0]));
//                System.out.println(fishState.getStep() + "YFT[0]= " + (totalAbundanceYFT[0][0]+totalAbundanceYFT[1][0]));


                assertEquals(totalAbundanceBET[0][0]+totalAbundanceBET[1][0], 7653070,10);
                assertEquals((totalAbundanceSKJ[0][0]+totalAbundanceSKJ[1][0])/100000, 154464,10);
                assertEquals(totalAbundanceYFT[0][0]+totalAbundanceYFT[1][0], 299183000,10);



//                System.out.println("breakpoint");

            }
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }
}
