package uk.ac.ox.poseidon.r;

import uk.ac.ox.poseidon.common.Services;
import uk.ac.ox.poseidon.simulations.api.FileScenarioLoader;
import uk.ac.ox.poseidon.simulations.api.Scenario;
import uk.ac.ox.poseidon.simulations.api.Simulation;

import java.nio.file.Path;
import java.nio.file.Paths;

public class YamlScenarioLoader {
    public static void main(String[] args) {
        System.out.println("Working Directory = " + System.getProperty("user.dir"));

        final Path scenarioPath =
            Paths.get("/", "home", "nicolas",
                "workspace", "tuna", "np", "calibrations",
                "vps_holiday_runs", "without_betavoid_with_temp",
                "cenv0729", "2022-12-24_18.13.45_global", "calibrated_scenario.yaml"
            );
        final Scenario scenario = new YamlScenarioLoader().load(scenarioPath);
        final Simulation simulation = scenario.newSimulation();
        simulation.step();
    }

    public Scenario load(final String scenarioPath) {
        return load(Paths.get(scenarioPath));
    }

    public Scenario load(final Path scenarioPath) {
        return Services
            .loadFirst(
                FileScenarioLoader.class,
                scenarioLoader -> scenarioLoader.getSupportedExtensions().contains("yaml")
            )
            .load(scenarioPath);
    }
}
