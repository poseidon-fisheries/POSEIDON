package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomTrawlStringFactory;
import uk.ac.ox.oxfish.geography.mapmakers.OsmoseMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFishStateTimeSeries;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * What happens to a few fish biomasses in OSMOSE depending on # of fishers and gear used
 * Created by carrknight on 11/9/15.
 */
public class OsmoseComparativeStatics
{

    public static final int RUNS = 50;
    public static final int YEARS_PER_SIMULATION = 30;

    public static void main(String[] args)
    {
        Path root = Paths.get("runs", "osmose");
        root.toFile().mkdirs();
        //10 times virgin
        for(int run = 0; run< RUNS; run++)
        {
            File runFile = root.resolve("virgin_"+run+".csv").toFile();
            PrototypeScenario scenario = new PrototypeScenario();
            //osmose scenario with no fishers
            scenario.setBiologyInitializer(new OsmoseBiologyFactory());
            scenario.setMapInitializer(new OsmoseMapInitializerFactory());
            scenario.setFishers(0);
            scenario.setNetworkBuilder(new EmptyNetworkBuilder());

            //create and run
            FishState fishState = new FishState(System.currentTimeMillis());
            fishState.setScenario(scenario);

            fishState.start();
            while(fishState.getYear()< YEARS_PER_SIMULATION + 1)
                fishState.schedule.step(fishState);

            //print out all biomasses
            YearlyFishStateTimeSeries yearlyData = fishState.getYearlyDataSet();
            DataColumn[] data = new DataColumn[fishState.getSpecies().size()];
            for(int i=0; i<data.length; i++)
            {
                data[i] = yearlyData.getColumn( "Biomass " + fishState.getSpecies().get(i).getName());
            }

            FishStateUtilities.printCSVColumnsToFile(runFile,
                                                     data);
        }

        //10 times demersal 1
        for(int run = 0; run<RUNS; run++)
        {
            File runFile = root.resolve("dem1_"+run+".csv").toFile();
            PrototypeScenario scenario = new PrototypeScenario();
            //osmose scenario with no fishers
            scenario.setBiologyInitializer(new OsmoseBiologyFactory());
            scenario.setMapInitializer(new OsmoseMapInitializerFactory());
            scenario.setFishers(100);
            RandomTrawlStringFactory gear = new RandomTrawlStringFactory();
            gear.setCatchabilityMap("2:.01");
            scenario.setGear(gear);
            //scenario.setNetworkBuilder(new EmptyNetworkBuilder());

            //create and run
            FishState fishState = new FishState(System.currentTimeMillis());
            fishState.setScenario(scenario);

            fishState.start();
            while(fishState.getYear()< YEARS_PER_SIMULATION + 1)
                fishState.schedule.step(fishState);

            //print out all biomasses
            YearlyFishStateTimeSeries yearlyData = fishState.getYearlyDataSet();
            DataColumn[] data = new DataColumn[fishState.getSpecies().size()];
            for(int i=0; i<data.length; i++)
            {
                data[i] = yearlyData.getColumn( "Biomass " + fishState.getSpecies().get(i).getName());
            }

            FishStateUtilities.printCSVColumnsToFile(runFile,
                                                     data);
        }

        //10 times demersal 1
        for(int run = 0; run<RUNS; run++)
        {
            File runFile = root.resolve("dem2_"+run+".csv").toFile();
            PrototypeScenario scenario = new PrototypeScenario();
            //osmose scenario with no fishers
            scenario.setBiologyInitializer(new OsmoseBiologyFactory());
            scenario.setMapInitializer(new OsmoseMapInitializerFactory());
            scenario.setFishers(100);
            RandomTrawlStringFactory gear = new RandomTrawlStringFactory();
            gear.setCatchabilityMap("3:.01");
            scenario.setGear(gear);
            //scenario.setNetworkBuilder(new EmptyNetworkBuilder());

            //create and run
            FishState fishState = new FishState(System.currentTimeMillis());
            fishState.setScenario(scenario);

            fishState.start();
            while(fishState.getYear()< YEARS_PER_SIMULATION + 1)
                fishState.schedule.step(fishState);

            //print out all biomasses
            YearlyFishStateTimeSeries yearlyData = fishState.getYearlyDataSet();
            DataColumn[] data = new DataColumn[fishState.getSpecies().size()];
            for(int i=0; i<data.length; i++)
            {
                data[i] = yearlyData.getColumn( "Biomass " + fishState.getSpecies().get(i).getName());
            }

            FishStateUtilities.printCSVColumnsToFile(runFile,
                                                     data);
        }





    }


}
