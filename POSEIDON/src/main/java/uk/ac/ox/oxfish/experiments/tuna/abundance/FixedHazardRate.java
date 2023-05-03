package uk.ac.ox.oxfish.experiments.tuna.abundance;

import uk.ac.ox.oxfish.maximization.TunaEvaluator;

import java.nio.file.Path;
import java.nio.file.Paths;

public class FixedHazardRate {

    private static final Path scenario =
        Paths.get(
            System.getProperty("user.home"),
            "workspace", "tuna", "calibration", "results",
            "ernesto", "2022-07-18 catchability_fixed_hazard_rate",
            "scenario.yaml"
        );
    private static final Path calibration = scenario.getParent().resolve("test.yaml");

    public static void main(String[] args) {
        TunaEvaluator evaluator = new TunaEvaluator(scenario, calibration);
        evaluator.run();
    }
}
