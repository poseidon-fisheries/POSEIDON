package uk.ac.ox.oxfish.experiments;

import com.esotericsoftware.minlog.Log;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by carrknight on 5/18/17.
 */
public class TacMixedKitchenSinkComparison {


    private final static Map<String,Path> scenarios = new HashMap<>();
    public static final long SEED = 0l;

    static {
        scenarios.put("tac",
                      Paths.get("docs",
                                "20170511 optimisation_remake",
                                "best",
                                "best-tac-mixed.yaml")
                      );
        scenarios.put("kitchensink",
                      Paths.get("docs",
                                "20170511 optimisation_remake",
                                "best",
                                "kitchensink-best.yaml"));
    }




    private final static Path outputFolder =
            Paths.get("docs",
                      "20170511 optimisation_remake",
                      "best","comparison");



    public static void main(String[] args) throws FileNotFoundException
    {


        Log.set(Log.LEVEL_INFO);

        for( Map.Entry<String,Path> scenario : scenarios.entrySet() )
        {

            FishYAML yaml = new FishYAML();
            PrototypeScenario scenario1 = yaml.loadAs(
                    new FileReader(scenario.getValue().toFile()), PrototypeScenario.class
            );
            FishState state = new FishState(SEED);
            state.setScenario(scenario1);
            state.start();
            while(state.getYear()<20)
                state.schedule.step(state);
            Log.info(scenario.getKey());

            FishStateUtilities.printCSVColumnsToFile(
                    outputFolder.resolve(scenario.getKey()+".csv").toFile(),
                    state.getYearlyDataSet().getColumn("Species 0 Landings"),
                    state.getYearlyDataSet().getColumn("Species 1 Landings"),
                    state.getYearlyDataSet().getColumn("Species 0 Recruitment"),
                    state.getYearlyDataSet().getColumn("Species 1 Recruitment"),
                    state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                    state.getYearlyDataSet().getColumn("Total Effort"),
                    state.getYearlyDataSet().getColumn("Biomass Species 0"),
                    state.getYearlyDataSet().getColumn("Biomass Species 1")
            );

            double score = 0;
            for(double landings :state.getYearlyDataSet().getColumn("Species 0 Landings") )
                score+=landings;
            score+= state.getYearlyDataSet().getColumn("Biomass Species 1").getLatest();

            Log.info("score: " + score);
        }



    }

}
