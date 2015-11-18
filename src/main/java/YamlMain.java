import com.esotericsoftware.minlog.Log;
import org.cobbzilla.util.yml.YmlMerger;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateLogger;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileWriter;
import java.io.IOException;
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
        String overwriter = args[1];

        Path outputFolder = Paths.get("output", overwriter.split("\\.")[0]);
        outputFolder.toFile().mkdirs();

        //create scenario and files
        YmlMerger merger = new YmlMerger();
        String fullScenario = merger.mergeToString(new String[]{baseInput, overwriter});

        FishYAML yaml = new FishYAML();
        PrototypeScenario scenario = yaml.loadAs(fullScenario,PrototypeScenario.class);

        System.out.println(scenario.getBiologyInitializer().getClass().toString());
        FishState model = new FishState(System.currentTimeMillis());
        Log.setLogger(new FishStateLogger(model,Paths.get("log.txt")));
        Log.set(Log.LEVEL_INFO);
        model.setScenario(scenario);
        model.start();
        while(model.getYear()<20)
            model.schedule.step(model);

        yaml.dump(scenario,new FileWriter(outputFolder.resolve("scenario.yaml").toFile()));
        FileWriter writer = new FileWriter(outputFolder.resolve("result.txt").toFile());
        writer.write(model.getLatestYearlyObservation("Biomass " + model.getSpecies().get(1).getName()).toString());
        writer.flush();
        writer.close();


    }

}
