import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.OneSpecieGearFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.oxfish.utility.yaml.ModelResults;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Created by carrknight on 11/18/15.
 */
public class YamlMain {

    public static void main(String[] args) throws IOException {



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
        model.setScenario(scenario);
        model.start();
        while(model.getYear()<20)
            model.schedule.step(model);

        FileWriter writer = new FileWriter(outputFolder.resolve("result.txt").toFile());
        //remaining stock of species 1 + total landings of species 2
        Double fitness = model.getLatestYearlyObservation(
                "Biomass " + model.getSpecies().get(1).getName()) +
                model.getYearlyDataSet().getColumn(model.getSpecies().get(0) + " " +
                                                           AbstractMarket.LANDINGS_COLUMN_NAME).stream().reduce(0d,
                                                                                                                (aDouble, aDouble2) -> aDouble+aDouble2);
        writer.write(fitness.toString());
        writer.flush();
        writer.close();

        writer = new FileWriter(outputFolder.resolve("result.yaml").toFile());
        ModelResults results =  new ModelResults(model);
        yaml.dump(results,writer);


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
                                                              if(friend.getID() < 50 && fisher.getID() < 50)
                                                                  sameGearConnections++;
                                                              else  if (friend.getID() >= 50 && fisher.getID() >= 50)
                                                                  sameGearConnections++;
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

}
