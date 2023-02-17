package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedMap;
import junit.framework.TestCase;
import sim.field.grid.DoubleGrid2D;
import uk.ac.ox.oxfish.environment.EnvironmentalLayer;
import uk.ac.ox.oxfish.environment.EnvironmentalMapFactory;
import uk.ac.ox.oxfish.environment.SimpleGridsSupplier;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.AdditionalMapFactory;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.Map;

import static org.junit.Assert.*;
import static uk.ac.ox.oxfish.model.scenario.TestableScenario.startTestableScenario;

public class EpoScenarioPathfindingTest extends TestCase {

    public void testGenericGrid(){
        EnvironmentalLayer environmentalLayer = new EnvironmentalLayer();
        environmentalLayer.addEnvironmentalMap(new EnvironmentalMapFactory("maxShear","inputs/epo_inputs/currents/maxShear.csv"));

        final FishState fishState = new FishState();

        System.out.println("break");

    }

    public void testEpoScenarioPathfinding() throws Exception{
        FishYAML yaml = new FishYAML();

        final EpoScenarioPathfinding scenario = yaml.loadAs(
                new FileReader(
                        Paths.get("inputs","epo_inputs","calibration","EPO_dev_scenario_BP.yaml").toFile()),
                EpoScenarioPathfinding.class
        );
        final FishState fishState = new FishState();
        fishState.setScenario(scenario);

        EnvironmentalLayer environmentalLayer = new EnvironmentalLayer();
        environmentalLayer.addEnvironmentalMap(new EnvironmentalMapFactory("maxShear","inputs/epo_inputs/currents/maxShear.csv"));
        AdditionalStartable shearMap = environmentalLayer.getEnvironmentalMap("maxShear").apply(fishState);
        fishState.registerStartable(shearMap);
//        shearMap.start(fishState);
        fishState.start();

        //       System.out.println("break");

        do {
            fishState.schedule.step(fishState);
/*            if(fishState.getStep() %30== 1){
                System.out.println(fishState.getStep());
                DoubleGrid2D shearGrid = fishState.getMap().getAdditionalMaps().get("maxShear").get();

 //               System.out.println("break");

            }*/
        } while (fishState.getYear() < 1);
    }
}
