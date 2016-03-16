package uk.ac.ox.oxfish;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.FuelTank;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.OneSpecieGearFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.RandomCatchabilityTrawlFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.network.EquidegreeBuilder;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.oxfish.utility.yaml.ModelResults;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.ToDoubleFunction;

/**
 * Created by carrknight on 11/18/15.
 */
public class YamlMain {

    public static void main(String[] args) throws IOException {



        /**
         * the first two arguments are the base scenario file and a yaml overwriter
         */
        Path inputFolder = Paths.get(args[0]);

        Long seed = args.length>1 ? Long.parseLong(args[1]) : System.currentTimeMillis();

        String simulationName = inputFolder.getFileName().toString();
        simulationName = simulationName.split("\\.")[0];
        Path outputFolder = Paths.get("output", simulationName);
        outputFolder.toFile().mkdirs();

        //create scenario and files
        String fullScenario = String.join("\n", Files.readAllLines(inputFolder));

        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(fullScenario,PrototypeScenario.class);
        yaml.dump(scenario,new FileWriter(outputFolder.resolve("scenario.yaml").toFile()));

        FishState model = new FishState(seed);
        Log.setLogger(new FishStateLogger(model,
                                          outputFolder.resolve(simulationName+ "_log.txt")));
        Log.set(Log.LEVEL_ERROR);
        model.setScenario(scenario);
        model.start();
        while(model.getYear()<20)
            model.schedule.step(model);

        FileWriter writer = new FileWriter(outputFolder.resolve("result.yaml").toFile());
        ModelResults results =  new ModelResults(model);
        yaml.dump(results,writer);

        writer = new FileWriter(outputFolder.resolve("seed.txt").toFile());
        writer.write(Long.toString(seed));
        writer.close();

    }

    public static void unfriend(String[] args) throws IOException {



        /**
         * the first two arguments are the base scenario file and a yaml overwriter
         */
        String baseInput = args[0];

        Path outputFolder = Paths.get("output", baseInput.split("\\.")[0]);
        outputFolder.toFile().mkdirs();

        //create scenario and files
        String fullScenario = String.join("\n",Files.readAllLines(Paths.get(baseInput)));

        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(fullScenario,PrototypeScenario.class);
        yaml.dump(scenario,new FileWriter(outputFolder.resolve("scenario.yaml").toFile()));

        OneSpecieGearFactory option1 = new OneSpecieGearFactory();
        option1.setSpecieTargetIndex(0);
        OneSpecieGearFactory option2 = new OneSpecieGearFactory();
        option2.setSpecieTargetIndex(1);

        scenario.setGear(new AlgorithmFactory<Gear>() {
            int counter;
            @Override
            public Gear apply(FishState fishState) {
                counter++;
                Log.info("counter: " + counter);
                if(counter<=50)
                    return option1.apply(fishState);
                else
                    return option2.apply(fishState);
            }
        });

        FishState model = new FishState(System.currentTimeMillis());
        Log.setLogger(new FishStateLogger(model,Paths.get(baseInput.split("\\.")[0] + "_log.txt")));
        Log.set(Log.LEVEL_ERROR);
        model.setScenario(scenario);
        model.start();

        model.getYearlyDataSet().registerGatherer("Same Gear Friends",
                                                  fishState -> {
                                                      double sameGearConnections = 0;
                                                      double connections = 0;
                                                      for(Fisher fisher : fishState.getFishers())
                                                      {
                                                          Collection<Fisher> friends = fisher.getDirectedFriends();
                                                          connections += friends.size();
                                                          for(Fisher friend : friends)
                                                          {
                                                              if(friend.getID() < 50 && fisher.getID() < 50) {
                                                                  sameGearConnections++;
                                                              }
                                                              else  if (friend.getID() >= 50 && fisher.getID() >= 50) {
                                                                  sameGearConnections++;
                                                              }
                                                          }

                                                      }
                                                     return sameGearConnections/connections;
                                                  },
                                                  Double.NaN);


        while(model.getYear()<20)
            model.schedule.step(model);



        FileWriter writer = new FileWriter(outputFolder.resolve("result.yaml").toFile());
        ModelResults results =  new ModelResults(model);
        yaml.dump(results,writer);


    }



    public static void twopopulations(String[] args) throws IOException {

        /**
         * the first two arguments are the base scenario file and a yaml overwriter
         */
        String baseInput = args[0];

        Path outputFolder = Paths.get("output", baseInput.split("\\.")[0]);
        outputFolder.toFile().mkdirs();

        //create scenario and files
        String fullScenario = String.join("\n",Files.readAllLines(Paths.get(baseInput)));

        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(fullScenario,PrototypeScenario.class);
        yaml.dump(scenario,new FileWriter(outputFolder.resolve("scenario.yaml").toFile()));

        FishState model = new FishState(System.currentTimeMillis());
        Log.setLogger(new FishStateLogger(model,Paths.get(baseInput.split("\\.")[0] + "_log.txt")));
        Log.set(Log.LEVEL_ERROR);


        EquidegreeBuilder builder = (EquidegreeBuilder) scenario.getNetworkBuilder();
        //connect people that have the same hold to avoid stupid imitation noise.
        builder.addPredicate((from, to) -> {
            return (from.getID() <50 && to.getID() < 50) ||   (from.getID() >=50 && to.getID() >= 50);
            //return Math.abs(from.getMaximumHold() - to.getMaximumHold()) < 1;
        });        scenario.setNetworkBuilder(builder);

        //with this startable I handicap the boats with low hold size to also have low fuel capacity and catchability
        //I don't really have an easier way to correlate parameters I am afraid
        model.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                LinkedList<Fisher> poorFishers = new LinkedList<Fisher>();
                for(Fisher fisher : model.getFishers())
                {
                    if(fisher.getID() <50)
                    {
                        //change their boat so they can't go very far
                        fisher.setBoat(new Boat(fisher.getBoat().getLength(),
                                                fisher.getBoat().getWidth(),
                                                fisher.getBoat().getEngine(),
                                                new FuelTank(500)));
                        fisher.setHold(new Hold(10, model.getSpecies().size()));
                        RandomCatchabilityTrawlFactory gearFactory = new RandomCatchabilityTrawlFactory();
                        gearFactory.setMeanCatchabilityFirstSpecies(new FixedDoubleParameter(.001));
                        fisher.setGear(gearFactory.apply(model));
                        fisher.setRegulation(new Anarchy());
                        poorFishers.add(fisher);
                    }
                }

                model.getYearlyDataSet().registerGatherer("Poor Fishers Total Income",
                                                          fishState -> poorFishers.stream().
                                                                  mapToDouble(new ToDoubleFunction<Fisher>() {
                                                                      @Override
                                                                      public double applyAsDouble(Fisher value) {
                                                                          return value.getLatestYearlyObservation(
                                                                                  YearlyFisherTimeSeries.CASH_COLUMN);
                                                                      }
                                                                  }).sum(), Double.NaN);
            }

            @Override
            public void turnOff() {

            }
        });
        model.setScenario(scenario);
        model.start();
        while(model.getYear()<20)
            model.schedule.step(model);


        FileWriter writer = new FileWriter(outputFolder.resolve("result.yaml").toFile());
        ModelResults results =  new ModelResults(model);
        yaml.dump(results,writer);

    }
}
