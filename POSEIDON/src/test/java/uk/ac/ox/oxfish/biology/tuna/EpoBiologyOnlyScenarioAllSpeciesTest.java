package uk.ac.ox.oxfish.biology.tuna;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.currents.CurrentPatternMapSupplier;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoAbundanceScenario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class EpoBiologyOnlyScenarioAllSpeciesTest {

    private final Path testInputs = Paths.get(
            "D:", "alexn", "Documents", "Oxford", "epo_biology_data", "biomass"
    );

    private final Path biologyTestFile = testInputs.resolve("biomass_test.csv");

    @Test
    public void testRunBiologyOnlyScenario() {
        final EpoAbundanceScenario scenario = new EpoAbundanceScenario();

        ((AbundanceMortalityProcessFromFileFactory)
                ((ScheduledAbundanceProcessesFactory) scenario.getBiologicalProcesses()
                        .getScheduledProcesses())
                        .getAbundanceMortalityProcess())
                .setMortalityFile(scenario.testFolder().path("mortality_BP.csv"));
        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        scenario.getFadMap().setCurrentPatternMapSupplier(CurrentPatternMapSupplier.EMPTY);

        fishState.start();

        System.out.println("SeaTiles: " + fishState.getMap().getAllSeaTiles().size());

        final Species bet = fishState.getSpecies("Bigeye tuna");

        System.out.println("breakpoint");

        Map<Integer, Double> expectedBiomass =
                recordStream(biologyTestFile)
                        .collect(toImmutableMap(record -> record.getInt("day"), record -> record.getDouble("biomass")));

        double[][] prevAbund = fishState.getTotalAbundance(bet);
        final double[][] deaths = fishState.getTotalAbundance(bet);

        System.out.println("check");

        do {
            System.out.println(fishState.getStep());
            if (expectedBiomass.containsKey(fishState.getStep())) {
                    System.out.println(fishState.getStep() + " " + fishState.getTotalBiomass(bet) / 1000);
                    final double[][] totalAbundance = fishState.getTotalAbundance(bet);
                    for (int i = 0; i < totalAbundance.length; i++) {
                        for (int j = 0; j < totalAbundance[0].length - 1; j++) {
                            deaths[i][j] = prevAbund[i][j] - totalAbundance[i][j + 1];
                        }
                    }
                    System.out.println("breakpoint");

                    prevAbund = fishState.getTotalAbundance(bet);

                    //System.out.println("new recruits: "+(totalAbundance[0][0]+totalAbundance[1][0]));
                    Assertions.assertEquals(expectedBiomass.get(fishState.getStep()), fishState.getTotalBiomass(bet), 1000000);
                }
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);
    }
}



