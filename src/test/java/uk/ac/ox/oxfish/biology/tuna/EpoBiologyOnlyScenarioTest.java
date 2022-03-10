package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableMap;
import junit.framework.TestCase;
import org.apache.commons.lang3.ArrayUtils;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoAbundanceScenario;
import uk.ac.ox.oxfish.model.scenario.EpoBiologyOnlyScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.util.List;

public class EpoBiologyOnlyScenarioTest extends TestCase {

    public void testRunBiologyOnlyScenario(){
        final EpoBiologyOnlyScenario scenario = new EpoBiologyOnlyScenario();
        final FishState fishState = new FishState();
        fishState.setScenario(scenario);
        scenario.getFadMapFactory().setCurrentFiles(ImmutableMap.of());

        fishState.start();

        System.out.println("SeaTiles: " + fishState.getMap().getAllSeaTiles().size());
        List<Species> specieses = fishState.getSpecies();

        Species BET = fishState.getSpecies("Bigeye tuna");
        assertEquals(fishState.getTotalBiomass(BET), 1470816000,1000);
        int[] checkDays = {-1,0,90,181,273};

        do {
            if(ArrayUtils.contains(checkDays,fishState.getStep())){
                System.out.println(fishState.getStep() + " " + fishState.getTotalBiomass(fishState.getSpecies("Bigeye tuna"))/1000);
                double[][] totalAbundance = fishState.getTotalAbundance(BET);
                System.out.println("breakpoint");
            }
            fishState.schedule.step(fishState);
        } while (fishState.getYear() < 1);

    }

}
