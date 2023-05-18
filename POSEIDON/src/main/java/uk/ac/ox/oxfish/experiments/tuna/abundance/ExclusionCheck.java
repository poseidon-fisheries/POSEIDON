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
    // private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/desperate_weibull/scenario.yaml");

    ///home/carrknight/Dropbox/oxfish_docs/20220725 currents/oldcurrents_blocked_test/runandgun/
    //private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/newcurrents_blocked_test/complete/rungun/scenario.yaml");
    //private static final Path scenarioOldCurrents = Paths.get("docs/20220725 currents/newcurrents_blocked_weibull/complete/rungun/scenario.yaml");
    private static final Path calibration = Paths.get(
        "docs/20220223 tuna_calibration/clorophill/neweez/manual1_fixed/manual.yaml");
    private static final Path scenario = Paths.get(
        "docs/20220223 tuna_calibration/clorophill/neweez/temperature_test3//scenario.yaml");

    public static void main(String[] args) {
        TunaEvaluator evaluator = new TunaEvaluator(
            scenario,
            calibration
        );
        evaluator.setNumRuns(0);
        evaluator.run();

    }
}
