import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;
import uk.ac.ox.oxfish.utility.yaml.ModelResults;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

}
