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
 * Created by carrknight on 5/23/17.
 */
public class KitchenSinkHalfComparison {


    private final static Map<String,Path> scenarios = new HashMap<>();
    public static final long SEED = 0l;
    public static final int NUMBER_OF_RUNS = 100;

    static {
        scenarios.put("tac",
                      Paths.get("docs",
                                "20170511 optimisation_remake",
                                "kitchensink","half","best",
                                "tac.yaml")
        );
        scenarios.put("kitchensink",
                      Paths.get("docs",
                                "20170511 optimisation_remake",
                                "kitchensink","half","best",
                                "kitchensink.yaml"));
        scenarios.put("mpa",
                      Paths.get("docs",
                                "20170511 optimisation_remake",
                                "kitchensink","half","best",
                                "mpa-only.yaml"));
        scenarios.put("season",
                      Paths.get("docs",
                                "20170511 optimisation_remake",
                                "kitchensink","half","best",
                                "season.yaml"));
        scenarios.put("itq",
                      Paths.get("docs",
                                "20170511 optimisation_remake",
                                "kitchensink","half","best",
                                "itq.yaml")
        );
    }




    private final static Path outputFolder =
            Paths.get("docs",
                      "20170511 optimisation_remake",
                      "kitchensink","half","best","comparison");



    public static void main(String[] args) throws FileNotFoundException
    {


        Log.set(Log.LEVEL_INFO);

        for(long run = SEED; run < SEED + NUMBER_OF_RUNS; run++) {

            for (Map.Entry<String, Path> scenario : scenarios.entrySet()) {

                FishYAML yaml = new FishYAML();
                PrototypeScenario scenario1 = yaml.loadAs(
                        new FileReader(scenario.getValue().toFile()), PrototypeScenario.class
                );
                FishState state = new FishState(run);
                state.setScenario(scenario1);
                state.start();
                while (state.getYear() < 20)
                    state.schedule.step(state);
                Log.info(scenario.getKey() + " " + run);

                FishStateUtilities.printCSVColumnsToFile(
                        outputFolder.resolve(scenario.getKey() +"_"+ run + ".csv").toFile(),
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
                for (double landings : state.getYearlyDataSet().getColumn("Species 0 Landings"))
                    score += landings;
                score += state.getYearlyDataSet().getColumn("Biomass Species 1").getLatest();

                Log.info("score: " + score);
            }
        }


    }

}
