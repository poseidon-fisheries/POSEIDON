package uk.ac.ox.oxfish.biology.tuna;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoAbundanceScenario;

import java.util.List;

public class EpoBiologyOnlyScenarioTest {

    @Test
    public void testRunBiologyOnlyScenario() {
        final EpoAbundanceScenario scenario = new EpoAbundanceScenario();


        ((AbundanceMortalityProcessFromFileFactory)
            ((ScheduledAbundanceProcessesFactory) scenario.getBiologicalProcessesFactory()
                .getScheduledProcessesFactory())
                .getAbundanceMortalityProcessFactory())
            .setMortalityFile(scenario.testFolder().path("mortality_BP.csv"));
        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        scenario.getFadMapFactory().setCurrentPatternMapSupplier(CurrentPatternMapSupplier.EMPTY);

        final double[] partialF = {0.132763, 0.00484204, 3.12056E-05, 0.0296353, 0.00197554, 0.00632267, 0.00559769, 0.0555887, 0.00258933};

        fishState.start();

        System.out.println("SeaTiles: " + fishState.getMap().getAllSeaTiles().size());
        final List<Species> specieses = fishState.getSpecies();

        final Species BET = fishState.getSpecies("Bigeye tuna");
        final double[][] initialAbundance = fishState.getTotalAbundance(BET);


        System.out.println("breakpoint");

//        assertEquals(fishState.getTotalBiomass(BET), 1472570250,1000);
        //this is coming out to be 1470816000

        final int[] checkDays = {0, 90, 181, 273};
        final double[] expectedBiomass = {1472570250,
            1263384168,
            1151702333,
            1047984966};

        int check = 0;
        double[][] prevAbund = fishState.getTotalAbundance(BET);
        final double[][] deaths = fishState.getTotalAbundance(BET);
        do {

            if (ArrayUtils.contains(checkDays, fishState.getStep())) {
                System.out.println(fishState.getStep() + " " + fishState.getTotalBiomass(fishState.getSpecies(
                    "Bigeye tuna")) / 1000);
                final double[][] totalAbundance = fishState.getTotalAbundance(BET);
                for (int i = 0; i < totalAbundance.length; i++) {
                    for (int j = 0; j < totalAbundance[0].length - 1; j++) {
                        deaths[i][j] = prevAbund[i][j] - totalAbundance[i][j + 1];
                    }
                }
                System.out.println("breakpoint");

                prevAbund = fishState.getTotalAbundance(BET);

                //System.out.println("new recruits: "+(totalAbundance[0][0]+totalAbundance[1][0]));
                Assert.assertEquals(expectedBiomass[check], fishState.getTotalBiomass(BET), 1000000);
                check++;
            }
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }
}
