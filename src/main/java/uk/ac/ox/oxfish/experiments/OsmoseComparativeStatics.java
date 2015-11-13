package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import ec.util.MersenneTwisterFast;
import sim.display.Console;
import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.RandomCatchabilityTrawl;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomTrawlStringFactory;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.GearImitationAnalysis;
import uk.ac.ox.oxfish.geography.mapmakers.OsmoseMapInitializerFactory;
import uk.ac.ox.oxfish.gui.FishGUI;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFishStateTimeSeries;
import uk.ac.ox.oxfish.model.network.EmptyNetworkBuilder;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.adaptation.Adaptation;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Predicate;

/**
 * What happens to a few fish biomasses in OSMOSE depending on # of fishers and gear used
 * Created by carrknight on 11/9/15.
 */
public class OsmoseComparativeStatics
{

    public static final int RUNS = 50;
    public static final int YEARS_PER_SIMULATION = 30;
    public static final Path ROOT = Paths.get("runs", "osmose");

    public static void secondaryEffects(String[] args)
    {
        ROOT.toFile().mkdirs();
        //10 times virgin
        for(int run = 0; run< RUNS; run++)
        {
            File runFile = ROOT.resolve("virgin_"+run+".csv").toFile();
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
            File runFile = ROOT.resolve("dem1_"+run+".csv").toFile();
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
            File runFile = ROOT.resolve("dem2_"+run+".csv").toFile();
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




    public static void main(String[] args) throws IOException {
        FishState model = new FishState(-1,1);
        Log.setLogger(new FishStateLogger(model,Paths.get("log.txt")));
        Log.set(Log.LEVEL_NONE);

        ROOT.toFile().mkdirs();


        int firstSpecies = 2;
        int secondSpecies = 3;
        PrototypeScenario scenario = new PrototypeScenario();

        scenario.setBiologyInitializer(new OsmoseBiologyFactory());
        scenario.setMapInitializer(new OsmoseMapInitializerFactory());
        scenario.setFishers(100);
        RandomTrawlStringFactory gear = new RandomTrawlStringFactory();
        gear.setCatchabilityMap(firstSpecies+":.01");
        scenario.setGear(gear);

        //no rules


        RandomTrawlStringFactory option1 = new RandomTrawlStringFactory();
        option1.setCatchabilityMap(firstSpecies+":.01");
        RandomTrawlStringFactory option2= new RandomTrawlStringFactory();
        option2.setCatchabilityMap(secondSpecies+":.01");
        model.registerStartable(new Startable() {
                                    @Override
                                    public void start(FishState model) {

                                        for (Fisher fisher : model.getFishers()) {

                                            Adaptation<Gear> trawlAdaptation =
                                                    new Adaptation<>(
                                                            (Predicate<Fisher>) fisher1 -> true,
                                                            new BeamHillClimbing<Gear>() {
                                                                @Override
                                                                public Gear randomStep(
                                                                        FishState state, MersenneTwisterFast random,
                                                                        Fisher fisher,
                                                                        Gear current) {
                                                                    return state.random.nextBoolean() ?
                                                                            option1.apply(state) :
                                                                            option2.apply(state);
                                                                }
                                                            },
                                                            GearImitationAnalysis.DEFAULT_GEAR_ACTUATOR,
                                                            fisher1 -> ((RandomCatchabilityTrawl) fisher1.getGear()),
                                                            new CashFlowObjective(365),
                                                            .1, .8);

                                            //tell the fisher to use this once a year
                                            fisher.addYearlyAdaptation(trawlAdaptation);
                                        }
                                        model.getYearlyDataSet().registerGatherer(model.getSpecies().get(firstSpecies)+ " Catchers", state1 -> {
                                            double size = state1.getFishers().size();
                                            if (size == 0)
                                                return Double.NaN;
                                            else {
                                                double total = 0;
                                                for (Fisher fisher1 : state1.getFishers())
                                                    total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[firstSpecies]
                                                            ;
                                                return total / .01;
                                            }
                                        }, Double.NaN);


                                        model.getYearlyDataSet().registerGatherer(model.getSpecies().get(secondSpecies) + " Catchers", state1 -> {
                                            double size = state1.getFishers().size();
                                            if (size == 0)
                                                return Double.NaN;
                                            else {
                                                double total = 0;
                                                for (Fisher fisher1 : state1.getFishers())
                                                    total += ((RandomCatchabilityTrawl) fisher1.getGear()).getCatchabilityMeanPerSpecie()[secondSpecies]
                                                            ;
                                                return total / .01;
                                            }
                                        }, Double.NaN);


                                    }

                                    /**
                                     * tell the startable to turnoff,
                                     */
                                    @Override
                                    public void turnOff() {

                                    }
                                }
        );



        //now work!
        model.setScenario(scenario);
        model.start();
        while(model.getYear() < 45)
            model.schedule.step(model);

        FishStateUtilities.printCSVColumnsToFile(ROOT.resolve("hardswitch.csv").toFile(),
                                                 model.getYearlyDataSet().getColumn(model.getSpecies().get(firstSpecies)+ " Catchers"),
                                                 model.getYearlyDataSet().getColumn(model.getSpecies().get(secondSpecies)+ " Catchers"),
                                                 model.getYearlyDataSet().getColumn( "Biomass " + model.getSpecies().get(firstSpecies).getName()),
                                                 model.getYearlyDataSet().getColumn( "Biomass " + model.getSpecies().get(secondSpecies).getName()));



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
