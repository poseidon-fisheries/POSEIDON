package uk.ac.ox.oxfish.experiments.tuna.abundance;

import uk.ac.ox.oxfish.maximization.TunaEvaluator;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ExclusionCheck {

//    private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/oldcurrents_blocked_test/scenario.yaml");
//    private static final Path calibrationOldCurrents = scenarioOldCurrents.getParent().resolve("test.yaml");

//    private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/newcurrents_blocked_test/scenario.yaml");
//    private static final Path calibrationOldCurrents = scenarioOldCurrents.getParent().resolve("test.yaml");
//    private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/oldcurrents_blocked_weibull/scenario.yaml");
//    private static final Path calibrationOldCurrents = scenarioOldCurrents.getParent().resolve("test.yaml");
 //   private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/newcurrents_blocked_weibull/scenario.yaml");
  //  private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/newcurrents_blocked_weibull/complete/scenario.yaml");
    //private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/newcurrents_blocked_test/complete/scenario.yaml");
    private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/desperate_weibull/scenario.yaml");
    private static final Path calibrationOldCurrents = scenarioOldCurrents.getParent().resolve("test.yaml");

    public static void main(String[] args){
        TunaEvaluator evaluator = new TunaEvaluator(scenarioOldCurrents,
                calibrationOldCurrents);
        evaluator.run();

    }
}
