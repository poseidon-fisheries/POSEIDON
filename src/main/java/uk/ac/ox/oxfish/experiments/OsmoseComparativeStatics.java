package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import sim.display.Console;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomTrawlStringFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.geography.mapmakers.OsmoseMapInitializerFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFishStateTimeSeries;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.regs.factory.MultiITQStringFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * What happens to a few fish biomasses in OSMOSE depending on # of fishers and gear used
 * Created by carrknight on 11/9/15.
 */
public class OsmoseComparativeStatics
{

    public static final int RUNS = 50;
    public static final int YEARS_PER_SIMULATION = 30;

    public static void secondaryEffects(String[] args)
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




    public static void osmoseITQGear(String[] args) throws IOException {
        FishState model = new FishState(-1,1);
        Log.setLogger(new FishStateLogger(model,Paths.get("log.txt")));
        Log.set(Log.LEVEL_TRACE);

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setMapMakerDedicatedRandomSeed(0l);
        scenario.setBiologyInitializer(new OsmoseBiologyFactory());
        scenario.setMapInitializer(new OsmoseMapInitializerFactory());
        scenario.setFishers(100);
        RandomTrawlStringFactory gear = new RandomTrawlStringFactory();
        gear.setCatchabilityMap("3:.01");
        scenario.setGear(gear);

        //mpa rules
        MultiITQStringFactory itqs = new MultiITQStringFactory();
        itqs.setYearlyQuotaMaps("3:1000");
        scenario.setUsePredictors(true);
        scenario.setRegulation(itqs);
        scenario.forcePortPosition(new int[]{1,1});

        RandomTrawlStringFactory option1 = new RandomTrawlStringFactory();
        option1.setCatchabilityMap("3:.01");
        RandomTrawlStringFactory option2= new RandomTrawlStringFactory();
        option1.setCatchabilityMap("2:.01");
        model.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                GearImitationAnalysis.attachGearAnalysisToEachFisher(model.getFishers(), model,
                                                                     Arrays.asList(option1.apply(model),option2.apply(model)),
                                                                     new CashFlowObjective(60));
            }

            @Override
            public void turnOff() {

            }
        });

        //sanity check: you either catch 2 or 3
        model.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                model.scheduleEveryDay(new Steppable() {
                    @Override
                    public void step(SimState simState)
                    {

                        for(Fisher fisher : model.getFishers())
                        {

                            if(!( Double.isNaN(fisher.predictDailyCatches(0)) ^
                                    ( fisher.predictDailyCatches(2) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(3) > FishStateUtilities.EPSILON) ^
                                    ( fisher.predictDailyCatches(2) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(3) < FishStateUtilities.EPSILON) ^
                                    ( fisher.predictDailyCatches(2) >FishStateUtilities.EPSILON && fisher.predictDailyCatches(3) < FishStateUtilities.EPSILON))) {
                                Preconditions.checkArgument(
                                        Double.isNaN(fisher.predictDailyCatches(0)) ^
                                                (fisher.predictDailyCatches(
                                                        2) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(
                                                        3) > FishStateUtilities.EPSILON) ^
                                                (fisher.predictDailyCatches(
                                                        2) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(
                                                        3) < FishStateUtilities.EPSILON) ^
                                                (fisher.predictDailyCatches(
                                                        2) > FishStateUtilities.EPSILON && fisher.predictDailyCatches(
                                                        3) < FishStateUtilities.EPSILON)
                                        , fisher.predictDailyCatches(2) + " ---- " + fisher.predictDailyCatches(
                                                3) + " ---- " +
                                                ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[2] + " , " +
                                                ((RandomCatchabilityTrawl) fisher.getGear()).getCatchabilityMeanPerSpecie()[3] + " <--- " +
                                                fisher.getID() + "\n"


                                );
                            }
                        }
                    }
                }, StepOrder.AFTER_DATA);
            }

            @Override
            public void turnOff() {

            }
        });


        //now work!
        model.setScenario(scenario);
        FishGUI gui = new FishGUI(model);
        Console c = new Console(gui);
        c.setVisible(true);



    }


    public static void mpaGUI(String[] args)
    {
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new OsmoseBiologyFactory());
        scenario.setMapInitializer(new OsmoseMapInitializerFactory());
        scenario.setFishers(100);
        RandomTrawlStringFactory gear = new RandomTrawlStringFactory();
        gear.setCatchabilityMap("3:.01");
        scenario.setGear(gear);

        //mpa rules
        scenario.setRegulation(new ProtectedAreasOnlyFactory());
        scenario.forcePortPosition(new int[]{1,1});

        //now work!
        FishState model = new FishState(System.currentTimeMillis(),1);
        model.setScenario(scenario);
        FishGUI gui = new FishGUI(model);
        Console c = new Console(gui);
        c.setVisible(true);


    }

}
